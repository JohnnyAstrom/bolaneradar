package com.bolaneradar.backend.controller.analytics;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.analytics.RateAnalyticsService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstester för RateAnalyticsController.
 * Uppdaterad för refaktorerad controller/service-struktur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateAnalyticsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RateAnalyticsService rateAnalyticsService;

    @Autowired
    BankService bankService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        RateAnalyticsService rateAnalyticsService() {
            return Mockito.mock(RateAnalyticsService.class);
        }

        @Bean
        BankService bankService() {
            return Mockito.mock(BankService.class);
        }
    }

    // ============================================================
    // TEST 1: /history/bank/{id} → Tom lista när bank saknas
    // ============================================================
    @Test
    void getRateHistory_returnsEmptyList_whenBankNotFound() throws Exception {

        // Service returnerar tom lista när bank inte finns
        when(rateAnalyticsService.getRateHistoryForBank(
                eq(99L),
                any(), any(),
                anyString(),
                any(), any()
        )).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/rates/analytics/history/bank/99")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ============================================================
    // TEST 2: /history/bank/{id} → returnerar historik
    // ============================================================
    @Test
    void getRateHistory_returnsList_whenBankExists() throws Exception {

        Bank bank = new Bank("Swedbank", "https://swedbank.se");
        bank.setId(1L);

        MortgageRate rate = new MortgageRate();
        rate.setBank(bank);
        rate.setRatePercent(BigDecimal.valueOf(3.85));
        rate.setRateType(RateType.LISTRATE);
        rate.setTerm(MortgageTerm.FIXED_1Y);
        rate.setEffectiveDate(LocalDate.of(2024, 1, 1));

        // Nya signaturen: service tar bankId, inte Bank
        when(rateAnalyticsService.getRateHistoryForBank(
                eq(1L),
                any(), any(),
                anyString(),
                any(), any()
        )).thenReturn(List.of(rate));

        mockMvc.perform(
                        get("/api/rates/analytics/history/bank/1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].ratePercent").value(3.85));
    }

    // ============================================================
    // TEST 3: /history/all-banks → returnerar map med banknamn
    // ============================================================
    @Test
    void getAllBanksRateHistory_returnsMapOfHistory() throws Exception {

        Bank bank1 = new Bank("SEB", "https://seb.se");
        Bank bank2 = new Bank("Nordea", "https://nordea.se");

        MortgageRate r1 = new MortgageRate();
        r1.setBank(bank1);
        r1.setRatePercent(BigDecimal.valueOf(4.0));
        r1.setTerm(MortgageTerm.FIXED_3Y);
        r1.setRateType(RateType.LISTRATE);
        r1.setEffectiveDate(LocalDate.of(2024, 1, 1));

        MortgageRate r2 = new MortgageRate();
        r2.setBank(bank2);
        r2.setRatePercent(BigDecimal.valueOf(3.5));
        r2.setTerm(MortgageTerm.FIXED_1Y);
        r2.setRateType(RateType.LISTRATE);
        r2.setEffectiveDate(LocalDate.of(2024, 1, 1));

        Map<String, List<MortgageRate>> mockMap = new LinkedHashMap<>();
        mockMap.put("SEB", List.of(r1));
        mockMap.put("Nordea", List.of(r2));

        // OBS: ny metodsignatur → 3 parametrar, inte 4
        when(rateAnalyticsService.getAllBanksRateHistory(
                any(), any(), anyString()
        )).thenReturn(mockMap);

        mockMvc.perform(
                        get("/api/rates/analytics/history/all-banks")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SEB[0].ratePercent").value(4.0))
                .andExpect(jsonPath("$.Nordea[0].ratePercent").value(3.5));
    }

    // ============================================================
    // TEST 4: /trends → returnerar lista av RateTrendDto
    // ============================================================
    @Test
    void getRateTrends_returnsList() throws Exception {

        RateTrend t = new RateTrend(
                "Swedbank",
                "FIXED_3Y",
                "LISTRATE",
                4.15,
                4.25,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1)
        );

        when(rateAnalyticsService.getRateTrends(
                any(), any(), any()
        )).thenReturn(List.of(t));

        mockMvc.perform(
                        get("/api/rates/analytics/trends")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].previousRate").value(4.15))
                .andExpect(jsonPath("$[0].currentRate").value(4.25));
    }

    // ============================================================
    // TEST 5: /trends/range → returnerar trender i intervall
    // ============================================================
    @Test
    void getRateTrendsInRange_returnsList() throws Exception {

        RateTrend t = new RateTrend(
                "Nordea",
                "FIXED_1Y",
                "LISTRATE",
                3.10,
                3.30,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 1)
        );

        when(rateAnalyticsService.getRateTrendsInRange(
                any(), any(), any()
        )).thenReturn(List.of(t));

        mockMvc.perform(
                        get("/api/rates/analytics/trends/range")
                                .param("from", "2024-01-01")
                                .param("to", "2024-03-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("Nordea"))
                .andExpect(jsonPath("$[0].previousRate").value(3.10))
                .andExpect(jsonPath("$[0].currentRate").value(3.30));
    }
}