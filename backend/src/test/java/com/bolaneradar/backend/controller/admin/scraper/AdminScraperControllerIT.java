package com.bolaneradar.backend.controller.admin.scraper;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminScraperControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ScraperService scraperService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        ScraperService scraperService() {
            return Mockito.mock(ScraperService.class);
        }
    }

    // ==========================================================
    // TEST 1: Lyckad scraping för alla banker
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeAllBanks_returnsSuccessMessage_whenServiceRunsWithoutError() throws Exception {

        // viktigt! annars körs riktiga scraping
        doNothing().when(scraperService).scrapeAllBanks();

        mockMvc.perform(post("/api/admin/scrape/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Scraping för alla banker slutförd."));
    }

    // ==========================================================
    // TEST 2: Misslyckad scraping för alla banker
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeAllBanks_returnsErrorMessage_whenServiceThrowsException() throws Exception {

        doThrow(new RuntimeException("Fel vid skrapning"))
                .when(scraperService).scrapeAllBanks();

        mockMvc.perform(post("/api/admin/scrape/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Fel vid scraping: Fel vid skrapning"));
    }

    // ==========================================================
    // TEST 3: Lyckad scraping för specifik bank
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeBank_returnsSuccessMessage_whenServiceSucceeds() throws Exception {

        when(scraperService.scrapeSingleBank("Swedbank"))
                .thenReturn("3 räntor sparade för Swedbank");

        mockMvc.perform(post("/api/admin/scrape/Swedbank"))
                .andExpect(status().isOk())
                .andExpect(content().string("3 räntor sparade för Swedbank"));
    }

    // ==========================================================
    // TEST 4: Misslyckad scraping för specifik bank
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeBank_returnsErrorMessage_whenServiceThrowsException() throws Exception {

        when(scraperService.scrapeSingleBank("Nordea"))
                .thenThrow(new Exception("Timeout vid hämtning"));

        mockMvc.perform(post("/api/admin/scrape/Nordea"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Fel vid scraping av Nordea: Timeout vid hämtning"));
    }
}