package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstester för ScraperController.
 *
 * Fokus:
 *  - Säkerställa att controllern hanterar scraping-anrop korrekt.
 *  - Kontrollera att rätt HTTP-status och meddelanden returneras.
 *  - Testa både lyckade och misslyckade scraping-scenarion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScraperControllerIT {

    @Autowired
    MockMvc mockMvc; // Används för att simulera HTTP-anrop i testmiljön

    @Autowired
    ScraperService scraperService; // Mockad version via TestConfig (nedan)

    /**
     * Lokal testkonfiguration som ersätter den riktiga ScraperService
     * med en Mockito-mock. På så sätt körs ingen verklig scraping.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        ScraperService scraperService() {
            return Mockito.mock(ScraperService.class);
        }
    }

    // ==========================================================
    // TEST 1: Lyckat scraping-scenario (alla banker)
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeAllBanks_returnsSuccessMessage_whenServiceRunsWithoutError() throws Exception {
        // Arrange – mocken gör ingenting, låtsas att scraping gick bra
        doNothing().when(scraperService).scrapeAllBanks();

        // Act + Assert – vi anropar controllern och kontrollerar svaret
        mockMvc.perform(post("/api/scrape/all"))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Scraping för alla banker slutförd"));
    }

    // ==========================================================
    // TEST 2: Misslyckat scraping-scenario (alla banker)
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeAllBanks_returnsErrorMessage_whenServiceThrowsException() throws Exception {
        // Arrange – mocken kastar ett fel för att simulera misslyckad scraping
        doThrow(new RuntimeException("Fel vid skrapning"))
                .when(scraperService).scrapeAllBanks();

        // Act + Assert – vi anropar controllern och förväntar oss felhantering
        mockMvc.perform(post("/api/scrape/all"))
                .andExpect(status().isInternalServerError()) // 500 Internal Server Error
                .andExpect(content().string("Ett fel uppstod vid scraping: Fel vid skrapning"));
    }

    // ==========================================================
    // TEST 3: Lyckat scraping-scenario för specifik bank
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeBankText_returnsSuccessMessage_whenServiceSucceeds() throws Exception {
        // Arrange – mocka ScraperService så att det returnerar en framgångstext
        when(scraperService.scrapeSingleBank("Swedbank"))
                .thenReturn("3 räntor sparade för Swedbank");

        // Act + Assert – anropa endpointen /api/scrape/Swedbank
        mockMvc.perform(post("/api/scrape/Swedbank"))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("3 räntor sparade för Swedbank"));
    }

    // ==========================================================
    // TEST 4: Misslyckat scraping-scenario för specifik bank
    // ==========================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void scrapeBankText_returnsErrorMessage_whenServiceThrowsException() throws Exception {
        // Arrange – mocka ScraperService så att det kastar ett Exception
        when(scraperService.scrapeSingleBank("Nordea"))
                .thenThrow(new Exception("Timeout vid hämtning"));

        // Act + Assert – anropa endpointen /api/scrape/Nordea
        mockMvc.perform(post("/api/scrape/Nordea"))
                .andExpect(status().isInternalServerError()) // 500 Internal Server Error
                .andExpect(content().string("Fel vid scraping av Nordea: Timeout vid hämtning"));
    }
}