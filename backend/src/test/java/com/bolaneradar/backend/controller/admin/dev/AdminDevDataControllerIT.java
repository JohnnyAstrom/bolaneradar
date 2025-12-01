package com.bolaneradar.backend.controller.admin.dev;

import com.bolaneradar.backend.service.admin.AdminDataService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // viktigt – annars aktiveras inte controllern
class AdminDevDataControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminDataService adminDataService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        AdminDataService adminDataService() {
            return Mockito.mock(AdminDataService.class);
        }
    }

    // ============================================================
    // POST /api/admin/dev/import-example
    // ============================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void importExampleData_returns201() throws Exception {

        mockMvc.perform(post("/api/admin/dev/import-example"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Exempeldata importerad."));

        verify(adminDataService, times(1)).importExampleData();
    }

    // ============================================================
    // DELETE /api/admin/dev/clear
    // ============================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void clearDatabase_returns200() throws Exception {

        mockMvc.perform(delete("/api/admin/dev/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Databasen har tömts."));

        verify(adminDataService, times(1)).clearDatabase();
    }

    // ============================================================
    // DELETE /api/admin/dev/delete-rates
    // ============================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteRatesForBank_returns200() throws Exception {

        when(adminDataService.deleteRatesForBank("Swedbank"))
                .thenReturn("Rensade 3 räntor.");

        mockMvc.perform(delete("/api/admin/dev/delete-rates")
                        .param("bankName", "Swedbank"))
                .andExpect(status().isOk())
                .andExpect(content().string("Rensade 3 räntor."));

        verify(adminDataService, times(1)).deleteRatesForBank("Swedbank");
    }
}