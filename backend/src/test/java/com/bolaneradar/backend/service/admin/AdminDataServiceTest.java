package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för AdminDataService.
 * Fokuserar på:
 *  - Att repository och services anropas korrekt
 *  - Att logiken körs utan databas
 */
@ExtendWith(MockitoExtension.class)
class AdminDataServiceTest {

    @Mock
    BankRepository bankRepository;

    @Mock
    MortgageRateRepository rateRepository;

    @Mock
    RateUpdateLogService rateUpdateLogService;

    @InjectMocks
    AdminDataService adminDataService;

    // ============================================================
    // clearDatabase()
    // ============================================================
    @Test
    void clearDatabase_deletesRatesAndLogs() {
        // Act
        adminDataService.clearDatabase();

        // Assert – se till att rätt metoder anropas
        verify(rateRepository).deleteAll();
        verify(rateUpdateLogService).clearAllLogs();
        verifyNoMoreInteractions(rateRepository, rateUpdateLogService);
    }

    // ============================================================
    // importExampleData() – banker skapas om de inte finns
    // ============================================================
    @Test
    void importExampleData_createsBanks_ifNotExisting() {
        // Arrange – ingen bank finns
        when(bankRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act
        adminDataService.importExampleData();

        // Assert – bankRepository.save() ska anropas flera gånger
        verify(bankRepository, atLeastOnce()).save(any(Bank.class));
    }

    @Test
    void importExampleData_doesNotCreateBanks_ifTheyExist() {
        // Arrange – alla banker finns redan
        when(bankRepository.findByName(anyString()))
                .thenReturn(Optional.of(new Bank("Swedbank")));

        // Act
        adminDataService.importExampleData();

        // Assert – save() ska aldrig anropas
        verify(bankRepository, never()).save(any());
    }

    // ============================================================
    // deleteRatesForBank()
    // ============================================================
    @Test
    void deleteRatesForBank_returnsMessage_whenBankNotFound() {
        // Arrange
        when(bankRepository.findByNameIgnoreCase("Okänd")).thenReturn(Optional.empty());

        // Act
        String result = adminDataService.deleteRatesForBank("Okänd");

        // Assert
        assertEquals("Ingen bank hittades med namn: Okänd", result);
        verify(rateRepository, never()).deleteByBank(any());
    }

    @Test
    void deleteRatesForBank_deletesRates_andReturnsCount() {
        // Arrange
        Bank bank = new Bank("Swedbank");

        when(bankRepository.findByNameIgnoreCase("Swedbank"))
                .thenReturn(Optional.of(bank));

        MortgageRate r1 = new MortgageRate();
        MortgageRate r2 = new MortgageRate();
        MortgageRate r3 = new MortgageRate();

        when(rateRepository.findByBank(bank))
                .thenReturn(List.of(r1, r2, r3));  // ✔ korrekt typ

        // Act
        String result = adminDataService.deleteRatesForBank("Swedbank");

        // Assert
        assertEquals("Rensade 3 räntor för Swedbank.", result);
        verify(rateRepository).deleteByBank(bank);
    }
}