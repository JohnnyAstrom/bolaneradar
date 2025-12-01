package com.bolaneradar.backend.controller.admin.rates;

import com.bolaneradar.backend.dto.admin.MortgageRateDto;
import com.bolaneradar.backend.service.admin.MortgageRateAdminService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminMortgageRateControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MortgageRateAdminService adminService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        MortgageRateAdminService mortgageRateAdminService() {
            return Mockito.mock(MortgageRateAdminService.class);
        }
    }

    // =====================================================
    // TEST 1: POST /api/admin/rates – OK (ADMIN)
    // =====================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createRates_returns201_whenAdminAndValidRequest() throws Exception {

        MortgageRateDto dto = new MortgageRateDto(
                null,
                "Swedbank",
                com.bolaneradar.backend.entity.enums.MortgageTerm.FIXED_3Y,
                com.bolaneradar.backend.entity.enums.RateType.LISTRATE,
                BigDecimal.valueOf(4.25),
                LocalDate.of(2025, 1, 1),
                null,
                null
        );

        when(adminService.createRates(anyList())).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/admin/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "bankName": "Swedbank",
                                  "term": "FIXED_3Y",
                                  "rateType": "LISTRATE",
                                  "ratePercent": 4.25,
                                  "effectiveDate": "2025-01-01"
                                }]
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].ratePercent").value(4.25));

        verify(adminService, times(1)).createRates(anyList());
    }

    // =====================================================
    // TEST 2: POST /api/admin/rates – ingen inloggning → 401
    // =====================================================
    @Test
    void createRates_returns401_whenUserNotLoggedIn() throws Exception {

        mockMvc.perform(post("/api/admin/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "bankName": "Swedbank",
                                  "term": "FIXED_3Y",
                                  "rateType": "LISTRATE",
                                  "ratePercent": 4.25,
                                  "effectiveDate": "2025-01-01"
                                }]
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminService);
    }

    // =====================================================
    // TEST 3: POST /api/admin/rates – inloggad user → 403
    // =====================================================
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void createRates_returns403_whenUserNotAdmin() throws Exception {

        mockMvc.perform(post("/api/admin/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "bankName": "Swedbank",
                                  "term": "FIXED_3Y",
                                  "rateType": "LISTRATE",
                                  "ratePercent": 4.25,
                                  "effectiveDate": "2025-01-01"
                                }]
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    // =====================================================
    // TEST 4: POST /api/admin/rates – tom lista → 400
    // =====================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createRates_returns400_whenListEmpty() throws Exception {

        mockMvc.perform(post("/api/admin/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService);
    }
}