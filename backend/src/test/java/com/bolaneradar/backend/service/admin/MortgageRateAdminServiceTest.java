package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.dto.admin.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.admin.MortgageRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;
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

@ExtendWith(MockitoExtension.class)
class MortgageRateAdminServiceTest {

    @Mock
    MortgageRateRepository rateRepository;

    @Mock
    BankService bankService;

    @InjectMocks
    MortgageRateAdminService adminService;

    // ============================================================
    // TEST 1: Lyckad skapning av räntor
    // ============================================================
    @Test
    void createRates_savesRatesAndReturnsDtos() {
        // Arrange
        Bank bank = new Bank("Swedbank");
        when(bankService.getBankByName("Swedbank"))
                .thenReturn(Optional.of(bank));

        MortgageRateDto dto = new MortgageRateDto(
                null,
                "Swedbank",
                MortgageTerm.FIXED_3Y,
                RateType.LISTRATE,
                BigDecimal.valueOf(4.25),
                LocalDate.now(),
                null,
                null
        );

        // Mockad entity som mappas och sparas
        MortgageRate mappedRate = MortgageRateMapper.toEntity(dto, bank);

        // Repository returnerar samma objekt vid save
        when(rateRepository.save(any(MortgageRate.class))).thenReturn(mappedRate);

        // Act
        List<MortgageRateDto> result = adminService.createRates(List.of(dto));

        // Assert
        assertEquals(1, result.size());
        assertEquals("Swedbank", result.get(0).bankName());
        assertEquals(BigDecimal.valueOf(4.25), result.get(0).ratePercent());

        verify(bankService).getBankByName("Swedbank");
        verify(rateRepository, times(1)).save(any(MortgageRate.class));
    }

    // ============================================================
    // TEST 2: Fel när bank saknas
    // ============================================================
    @Test
    void createRates_throwsException_whenBankDoesNotExist() {
        // Arrange
        MortgageRateDto dto = new MortgageRateDto(
                null,
                "FantomBank",
                MortgageTerm.FIXED_1Y,
                RateType.LISTRATE,
                BigDecimal.valueOf(3.5),
                LocalDate.now(),
                null,
                null
        );

        when(bankService.getBankByName("FantomBank"))
                .thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminService.createRates(List.of(dto))
        );

        assertEquals("Bank inte hittad: FantomBank", ex.getMessage());
        verify(rateRepository, never()).save(any());
    }
}