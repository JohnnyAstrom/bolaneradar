package com.bolaneradar.backend.controller.admin.logs;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import com.bolaneradar.backend.service.admin.RateUpdateLogService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminRateUpdateLogControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RateUpdateLogService rateUpdateLogService; // mock via TestConfig

    @TestConfiguration
    static class Config {
        @Bean
        RateUpdateLogService rateUpdateLogService() {
            return Mockito.mock(RateUpdateLogService.class);
        }
    }

    // ============================================================
    // GET /api/admin/rates/updates — alla loggar
    // ============================================================
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUpdateLogs_returnsListOfDtos() throws Exception {
        Bank bank = new Bank("Swedbank", "https://swedbank.se");

        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                "Scraper",
                3,
                bank,
                true,
                null,
                500L
        );

        when(rateUpdateLogService.getAllLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/rates/updates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("Swedbank"))
                .andExpect(jsonPath("$[0].importedCount").value(3))
                .andExpect(jsonPath("$[0].sourceName").value("Scraper"));
    }

    // ============================================================
    // GET /api/admin/rates/updates/latest — senaste per bank
    // ============================================================
    @Test
    @WithMockUser(roles = "ADMIN")
    void getLatestUpdatesPerBank_returnsDtos() throws Exception {

        Bank bank = new Bank("Nordea", "https://nordea.se");

        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                "Scraper",
                7,
                bank,
                true,
                null,
                900L
        );

        when(rateUpdateLogService.getLatestLogsPerBank()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/rates/updates/latest")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("Nordea"))
                .andExpect(jsonPath("$[0].sourceName").value("Scraper"))
                .andExpect(jsonPath("$[0].importedCount").value(7));
    }
}