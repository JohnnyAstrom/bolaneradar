package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för MortgageRateService.
 *
 * Fokus:
 *  - Testa logik utan att röra databasen
 *  - Säkerställa korrekt hantering av bankreferenser och mapper
 *  - Kontrollera att undantag hanteras korrekt
 */
@ExtendWith(MockitoExtension.class)
class MortgageRateServiceTest {

    @Mock
    MortgageRateRepository mortgageRateRepository;

    @Mock
    BankService bankService;

    @InjectMocks
    MortgageRateService mortgageRateService;

    // ===========================================================
    // TEST 1: createRate() - lyckat scenario
    // ===========================================================
    @Test
    void createRate_returnsDto_whenBankExists() {
        // Arrange
        Bank bank = new Bank("Swedbank");
        MortgageRateDto inputDto = new MortgageRateDto(
                null, "Swedbank", MortgageTerm.FIXED_3Y, RateType.LISTRATE,
                BigDecimal.valueOf(4.25), LocalDate.now(), null, null
        );

        when(bankService.getBankByName("Swedbank")).thenReturn(Optional.of(bank));
        when(mortgageRateRepository.save(any(MortgageRate.class)))
                .thenAnswer(inv -> inv.getArgument(0)); // returnera samma objekt

        // Act
        MortgageRateDto result = mortgageRateService.createRate(inputDto);

        // Assert
        assertEquals("Swedbank", result.bankName());
        assertEquals(BigDecimal.valueOf(4.25), result.ratePercent());
        verify(bankService).getBankByName("Swedbank");
        verify(mortgageRateRepository).save(any(MortgageRate.class));
    }

    // ===========================================================
    // TEST 2: createRate() - banken finns inte
    // ===========================================================
    @Test
    void createRate_throwsException_whenBankDoesNotExist() {
        // Arrange
        MortgageRateDto dto = new MortgageRateDto(
                null, "OkändBank", MortgageTerm.FIXED_1Y, RateType.LISTRATE,
                BigDecimal.valueOf(3.95), LocalDate.now(), null, null
        );

        when(bankService.getBankByName("OkändBank")).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mortgageRateService.createRate(dto)
        );

        assertEquals("Banken finns inte: OkändBank", ex.getMessage());
        verify(bankService).getBankByName("OkändBank");
        verifyNoInteractions(mortgageRateRepository);
    }

    // ===========================================================
    // TEST 3: getAllRates() - returnerar lista från repository
    // ===========================================================
    @Test
    void getAllRates_returnsListFromRepository() {
        // Arrange
        Bank bank = new Bank("Nordea");
        MortgageRate rate = new MortgageRate(bank, MortgageTerm.FIXED_3Y,
                RateType.LISTRATE, BigDecimal.valueOf(4.10), LocalDate.now());

        when(mortgageRateRepository.findAll()).thenReturn(List.of(rate));

        // Act
        List<MortgageRate> result = mortgageRateService.getAllRates();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Nordea", result.get(0).getBank().getName());
        verify(mortgageRateRepository).findAll();
    }

    // ===========================================================
    // TEST 4: getRatesByBank() - returnerar lista för en specifik bank
    // ===========================================================
    @Test
    void getRatesByBank_returnsRatesForGivenBank() {
        // Arrange
        Bank bank = new Bank("Handelsbanken");
        MortgageRate rate = new MortgageRate(bank, MortgageTerm.FIXED_1Y,
                RateType.LISTRATE, BigDecimal.valueOf(3.80), LocalDate.now());

        when(mortgageRateRepository.findByBank(bank)).thenReturn(List.of(rate));

        // Act
        List<MortgageRate> result = mortgageRateService.getRatesByBank(bank);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Handelsbanken", result.get(0).getBank().getName());
        verify(mortgageRateRepository).findByBank(bank);
    }

    // ===========================================================
    // TEST 5: getLatestRatesByType() - sortering per bank och term
    // ===========================================================
    @Test
    void getLatestRatesByType_sortsRatesCorrectly() {
        // Arrange
        Bank bankA = new Bank("SEB");
        Bank bankB = new Bank("Swedbank");

        MortgageRate rate1 = new MortgageRate(bankB, MortgageTerm.FIXED_3Y,
                RateType.LISTRATE, BigDecimal.valueOf(4.15), LocalDate.now());
        MortgageRate rate2 = new MortgageRate(bankA, MortgageTerm.FIXED_1Y,
                RateType.LISTRATE, BigDecimal.valueOf(4.05), LocalDate.now());

        when(mortgageRateRepository.findLatestRatesByType(RateType.LISTRATE))
                .thenReturn(List.of(rate1, rate2));

        // Act
        List<MortgageRate> sorted = mortgageRateService.getLatestRatesByType(RateType.LISTRATE);

        // Assert
        assertEquals("SEB", sorted.get(0).getBank().getName());
        assertEquals("Swedbank", sorted.get(1).getBank().getName());
        verify(mortgageRateRepository).findLatestRatesByType(RateType.LISTRATE);
    }
}