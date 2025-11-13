package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.MortgageRateService;
import com.bolaneradar.backend.service.analytics.RateAnalyticsService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstester för MortgageRateController.
 * Fokuserar på:
 *  - GET ska vara tillgängligt utan auth
 *  - POST kräver admin-användare
 *  - Felhantering via GlobalExceptionHandler
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MortgageRateControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MortgageRateService mortgageRateService;

    @Autowired
    BankService bankService;

    @Autowired
    RateAnalyticsService rateAnalyticsService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        MortgageRateService mortgageRateService() {
            return Mockito.mock(MortgageRateService.class);
        }

        @Bean
        BankService bankService() {
            return Mockito.mock(BankService.class);
        }

        @Bean
        RateAnalyticsService rateAnalyticsService() {
            return Mockito.mock(RateAnalyticsService.class);
        }
    }

    // =====================================================
    // TEST 1: GET /api/rates ska vara öppen och returnera JSON
    // =====================================================
    @Test
    void getAllRates_returnsJsonList_whenCalledWithoutAuth() throws Exception {
        // Arrange
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("Swedbank");

        MortgageRate rate = new MortgageRate();
        rate.setBank(bank); // viktigt pga mappern använder rate.getBank().getName()
        rate.setRatePercent(BigDecimal.valueOf(4.25));
        rate.setEffectiveDate(LocalDate.now());
        rate.setRateType(RateType.LISTRATE);
        rate.setTerm(MortgageTerm.FIXED_3Y);

        when(mortgageRateService.getAllRates()).thenReturn(List.of(rate));

        // Act + Assert
        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].ratePercent").value(4.25));
    }

    // =====================================================
    // TEST 2: GET /api/rates/bank/{id} returnerar 404 om bank saknas
    // =====================================================
    @Test
    void getRatesByBank_returns404_whenBankNotFound() throws Exception {
        when(bankService.getBankById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rates/bank/99"))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // TEST 3: GET /api/rates/bank/{id} returnerar lista om bank finns
    // =====================================================
    @Test
    void getRatesByBank_returnsList_whenBankExists() throws Exception {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("Swedbank");

        MortgageRate rate = new MortgageRate();
        rate.setBank(bank); // viktigt för mappern
        rate.setRatePercent(BigDecimal.valueOf(3.85));
        rate.setRateType(RateType.LISTRATE);
        rate.setTerm(MortgageTerm.FIXED_1Y);
        rate.setEffectiveDate(LocalDate.of(2025, 1, 1));

        when(bankService.getBankById(1L)).thenReturn(Optional.of(bank));
        when(mortgageRateService.getRatesByBank(bank)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/rates/bank/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].ratePercent").value(3.85));
    }

    // =====================================================
    // TEST 4: POST /api/rates kräver auth och returnerar 201 Created
    // =====================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void createRates_returns201_whenValidRequest() throws Exception {
        MortgageRateDto input = new MortgageRateDto(
                null, "Swedbank", MortgageTerm.FIXED_3Y, RateType.LISTRATE,
                BigDecimal.valueOf(4.20), LocalDate.now(), null, null
        );

        when(mortgageRateService.createRate(any(MortgageRateDto.class)))
                .thenReturn(input);

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "bankName": "Swedbank",
                                  "term": "FIXED_3Y",
                                  "rateType": "LISTRATE",
                                  "ratePercent": 4.20,
                                  "effectiveDate": "2025-01-01"
                                }]
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].ratePercent").value(4.20));
    }

    // =====================================================
    // TEST 5: POST /api/rates returnerar 400 vid ogiltig bank
    // =====================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void createRates_returns400_whenBankDoesNotExist() throws Exception {
        when(mortgageRateService.createRate(any()))
                .thenThrow(new IllegalArgumentException("Banken finns inte: OkändBank"));

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "bankName": "OkändBank",
                                  "term": "FIXED_3Y",
                                  "rateType": "LISTRATE",
                                  "ratePercent": 4.10,
                                  "effectiveDate": "2025-01-01"
                                }]
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Banken finns inte: OkändBank"));
    }
}