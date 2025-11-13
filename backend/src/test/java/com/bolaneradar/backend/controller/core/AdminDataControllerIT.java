package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.service.core.AdminDataService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstester för AdminDataController.
 * <p>
 * Fokus:
 *  - Säkerställa att endpoints anropas korrekt via HTTP
 *  - Verifiera att AdminDataService anropas
 *  - Testa både lyckade och fel-scenarion
 *  - Endast ADMIN-användare ska kunna anropa dessa endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminDataControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminDataService adminDataService; // Mockad via TestConfig

    /**
     * Lokal testkonfiguration som ersätter den riktiga AdminDataService
     * med en Mockito-mock. På så sätt rör vi aldrig riktig data.
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        AdminDataService adminDataService() {
            return Mockito.mock(AdminDataService.class);
        }
    }

    // ============================================================
    // TEST 1: POST /api/admin/import-example – lyckat scenario
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void importExampleData_returns201_whenServiceRunsSuccessfully() throws Exception {
        // Arrange
        doNothing().when(adminDataService).importExampleData();

        // Act + Assert
        mockMvc.perform(post("/api/admin/import-example"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Exempeldata importerad framgångsrikt!"));

        verify(adminDataService, times(1)).importExampleData();
    }

    // ============================================================
    // TEST 2: POST /api/admin/import-example – service kastar fel
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void importExampleData_returns500_whenServiceThrowsException() throws Exception {
        // Arrange – simulera oväntat fel i service-lagret
        doThrow(new RuntimeException("Fel vid import"))
                .when(adminDataService).importExampleData();

        // Act + Assert
        mockMvc.perform(post("/api/admin/import-example"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error")
                        .value("Ett oväntat fel inträffade: Fel vid import"));
    }

    // ============================================================
    // TEST 3: DELETE /api/admin/clear – lyckat scenario
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void clearDatabase_returns200_whenServiceRunsSuccessfully() throws Exception {
        // Arrange
        doNothing().when(adminDataService).clearDatabase();

        // Act + Assert
        mockMvc.perform(delete("/api/admin/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Databasen har tömts."));

        verify(adminDataService, times(1)).clearDatabase();
    }

    // ============================================================
    // TEST 4: DELETE /api/admin/clear – service kastar fel
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void clearDatabase_returns500_whenServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Fel vid rensning"))
                .when(adminDataService).clearDatabase();

        // Act + Assert
        mockMvc.perform(delete("/api/admin/clear"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error")
                        .value("Ett oväntat fel inträffade: Fel vid rensning"));
    }

    // ============================================================
    // TEST 5: DELETE /api/admin/delete-rates – lyckat scenario
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteRatesForBank_returnsMessageFromService_whenSuccessful() throws Exception {
        // Arrange
        when(adminDataService.deleteRatesForBank("Swedbank"))
                .thenReturn("Rensade 3 räntor för Swedbank.");

        // Act + Assert
        mockMvc.perform(delete("/api/admin/delete-rates")
                        .param("bankName", "Swedbank"))
                .andExpect(status().isOk())
                .andExpect(content().string("Rensade 3 räntor för Swedbank."));

        verify(adminDataService, times(1))
                .deleteRatesForBank("Swedbank");
    }

    // ============================================================
    // TEST 6: DELETE /api/admin/delete-rates – service kastar fel
    // ============================================================
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteRatesForBank_returns500_whenServiceThrowsException() throws Exception {
        // Arrange
        when(adminDataService.deleteRatesForBank("Nordea"))
                .thenThrow(new RuntimeException("Fel vid radering"));

        // Act + Assert
        mockMvc.perform(delete("/api/admin/delete-rates")
                        .param("bankName", "Nordea"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error")
                        .value("Ett oväntat fel inträffade: Fel vid radering"));
    }
}