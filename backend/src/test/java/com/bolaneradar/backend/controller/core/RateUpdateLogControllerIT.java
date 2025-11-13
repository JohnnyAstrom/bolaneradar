package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateUpdateLogControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RateUpdateLogService rateUpdateLogService;

    @Autowired
    BankService bankService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        RateUpdateLogService rateUpdateLogService() {
            return Mockito.mock(RateUpdateLogService.class);
        }

        @Bean
        BankService bankService() {
            return Mockito.mock(BankService.class);
        }
    }

    // =====================================================
    // GET /api/rates/updates – alla loggar
    // =====================================================
    @Test
    void getAllUpdateLogs_returnsListOfLogs() throws Exception {
        Bank bank = new Bank("Swedbank", "https://swedbank.se");

        RateUpdateLog log1 = new RateUpdateLog(
                LocalDateTime.now(),
                "ScraperService",
                5,
                bank,
                true,
                null,
                1200L
        );

        RateUpdateLog log2 = new RateUpdateLog(
                LocalDateTime.now().minusHours(1),
                "ManualImport",
                2,
                bank,
                false,
                "Fel vid import",
                800L
        );

        when(rateUpdateLogService.getAllLogs()).thenReturn(List.of(log1, log2));

        mockMvc.perform(get("/api/rates/updates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sourceName").value("ScraperService"))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].importedCount").value(5));
    }

    // =====================================================
    // GET /api/rates/updates/bank/{bankId} – bank saknas
    // =====================================================
    @Test
    void getLogsForBank_returns404_whenBankNotFound() throws Exception {
        when(bankService.getBankById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rates/updates/bank/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // GET /api/rates/updates/bank/{bankId} – bank finns
    // =====================================================
    @Test
    void getLogsForBank_returnsList_whenBankExists() throws Exception {
        Bank bank = new Bank("SEB", "https://seb.se");
        bank.setId(1L);

        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                "ScraperService",
                7,
                bank,
                true,
                null,
                1500L
        );

        when(bankService.getBankById(1L)).thenReturn(Optional.of(bank));
        when(rateUpdateLogService.getLogsForBank(bank)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/rates/updates/bank/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bankName").value("SEB"))
                .andExpect(jsonPath("$[0].sourceName").value("ScraperService"))
                .andExpect(jsonPath("$[0].importedCount").value(7));
    }

    // =====================================================
    // GET /api/rates/updates/latest – senaste per bank
    // =====================================================
    @Test
    void getLatestUpdatesPerBank_returnsLatestLogPerBank() throws Exception {
        Bank bank1 = new Bank("Nordea", "https://nordea.se");
        Bank bank2 = new Bank("Länsförsäkringar", "https://lf.se");

        RateUpdateLog log1 = new RateUpdateLog(
                LocalDateTime.now(),
                "ScraperService",
                10,
                bank1,
                true,
                null,
                1100L
        );

        RateUpdateLog log2 = new RateUpdateLog(
                LocalDateTime.now().minusMinutes(5),
                "ScraperService",
                4,
                bank2,
                true,
                null,
                900L
        );

        when(rateUpdateLogService.getLatestLogsPerBank())
                .thenReturn(List.of(log1, log2));

        mockMvc.perform(get("/api/rates/updates/latest")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bankName").value("Nordea"))
                .andExpect(jsonPath("$[1].bankName").value("Länsförsäkringar"));
    }
}