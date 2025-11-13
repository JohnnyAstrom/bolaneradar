package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.BankDto;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.service.core.BankService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BankControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BankService bankService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        BankService bankService() {
            return Mockito.mock(BankService.class);
        }
    }

    // =====================================================
    // GET /api/banks — returnerar alla banker
    // =====================================================
    @Test
    void getAllBanks_returnsBankList() throws Exception {

        Bank bank1 = new Bank("SEB", "https://seb.se");
        bank1.setId(1L);

        Bank bank2 = new Bank("Swedbank", "https://swedbank.se");
        bank2.setId(2L);

        when(bankService.getAllBanks()).thenReturn(List.of(bank1, bank2));

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("SEB"))
                .andExpect(jsonPath("$[1].name").value("Swedbank"));
    }

    // =====================================================
    // GET /api/banks/{id} — bank hittas
    // =====================================================
    @Test
    void getBankById_returns200_whenBankExists() throws Exception {

        Bank bank = new Bank("Nordea", "https://nordea.se");
        bank.setId(10L);

        when(bankService.getBankById(10L)).thenReturn(Optional.of(bank));

        mockMvc.perform(get("/api/banks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Nordea"));
    }

    // =====================================================
    // GET /api/banks/{id} — bank saknas → 404
    // =====================================================
    @Test
    void getBankById_returns404_whenNotFound() throws Exception {

        when(bankService.getBankById(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/banks/123"))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // POST /api/banks — skapa bank
    // =====================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createBank_returns201_whenValidRequest() throws Exception {

        BankDto input = new BankDto(null, "Handelsbanken", "https://handelsbanken.se", null);

        Bank saved = new Bank("Handelsbanken", "https://handelsbanken.se");
        saved.setId(99L);

        when(bankService.saveBank(any(Bank.class))).thenReturn(saved);

        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Handelsbanken",
                                  "website": "https://handelsbanken.se"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Handelsbanken"))
                .andExpect(jsonPath("$.id").value(99L));
    }

    // =====================================================
    // DELETE /api/banks/{id} — alltid 204
    // =====================================================
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteBank_returns204() throws Exception {

        mockMvc.perform(delete("/api/banks/5"))
                .andExpect(status().isNoContent());
    }
}