package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för AdminDataService.
 * <p>
 * Fokus:
 *  - Säkerställa att databasen rensas på rätt sätt
 *  - Säkerställa att räntor för en specifik bank rensas korrekt
 *  - Lätt sanity-check för importExampleData (att banker skapas vid behov)
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
    // TEST 1: clearDatabase() – rensar räntor och loggar
    // ============================================================
    @Test
    void clearDatabase_deletesAllRatesAndLogs() {
        // Act
        adminDataService.clearDatabase();

        // Assert
        // Först ska alla räntor tas bort
        verify(rateRepository, times(1)).deleteAll();
        // Sedan ska loggar rensas via RateUpdateLogService
        verify(rateUpdateLogService, times(1)).clearAllLogs();
    }

    // ============================================================
    // TEST 2: deleteRatesForBank() – bank hittas inte
    // ============================================================
    @Test
    void deleteRatesForBank_returnsMessage_whenBankNotFound() {
        // Arrange
        when(bankRepository.findByNameIgnoreCase("OkändBank"))
                .thenReturn(Optional.empty());

        // Act
        String result = adminDataService.deleteRatesForBank("OkändBank");

        // Assert
        assertEquals("Ingen bank hittades med namn: OkändBank", result);
        verify(bankRepository).findByNameIgnoreCase("OkändBank");
        verifyNoInteractions(rateRepository);
    }

    // ============================================================
    // TEST 3: deleteRatesForBank() – bank hittas och räntor rensas
    // ============================================================
    @Test
    void deleteRatesForBank_deletesRatesAndReturnsCorrectMessage_whenBankExists() {
        // Arrange
        Bank bank = new Bank("SEB");
        bank.setId(1L);

        when(bankRepository.findByNameIgnoreCase("SEB"))
                .thenReturn(Optional.of(bank));

        // Skapa några dummy-räntor för att simulera befintliga poster
        MortgageRate rate1 = new MortgageRate(
                bank,
                MortgageTerm.FIXED_1Y,
                RateType.LISTRATE,
                BigDecimal.valueOf(3.5),
                LocalDate.now()
        );
        MortgageRate rate2 = new MortgageRate(
                bank,
                MortgageTerm.FIXED_3Y,
                RateType.LISTRATE,
                BigDecimal.valueOf(3.8),
                LocalDate.now()
        );

        when(rateRepository.findByBank(bank))
                .thenReturn(List.of(rate1, rate2));

        // Act
        String result = adminDataService.deleteRatesForBank("SEB");

        // Assert
        // 2 räntor ska ha räknats innan delete
        assertEquals("Rensade 2 räntor för SEB.", result);
        verify(rateRepository, times(1)).deleteByBank(bank);
    }

    // ============================================================
    // TEST 4: importExampleData() – skapar banker om de saknas
    // ============================================================
    @Test
    void importExampleData_createsBanks_whenTheyDoNotExist() {
        // Arrange
        // Alla findByName() returnerar tomt → alla banker ses som nya
        when(bankRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        // Act
        adminDataService.importExampleData();

        // Assert
        // Vi bryr oss inte om exakt hur många banker, bara att minst en sparas
        verify(bankRepository, atLeastOnce()).save(any(Bank.class));
    }
}