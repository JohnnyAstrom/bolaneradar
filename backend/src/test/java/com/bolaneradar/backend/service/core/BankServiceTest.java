package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för BankService.
 * <p>
 * Fokus:
 *  - Testa logik utan databas
 *  - Säkerställa att repository anropas korrekt
 */
@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    BankRepository bankRepository;

    @InjectMocks
    BankService bankService;

    // ============================================================
    // getAllBanks()
    // ============================================================
    @Test
    void getAllBanks_returnsAllBanksFromRepository() {
        // Arrange
        Bank bank1 = new Bank("Swedbank");
        Bank bank2 = new Bank("Nordea");
        when(bankRepository.findAll()).thenReturn(List.of(bank1, bank2));

        // Act
        List<Bank> result = bankService.getAllBanks();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Swedbank", result.get(0).getName());
        assertEquals("Nordea", result.get(1).getName());
        verify(bankRepository).findAll();
    }

    // ============================================================
    // getBankById()
    // ============================================================
    @Test
    void getBankById_returnsBank_whenExists() {
        // Arrange
        Bank bank = new Bank("SEB");
        bank.setId(1L);
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));

        // Act
        Optional<Bank> result = bankService.getBankById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("SEB", result.get().getName());
        verify(bankRepository).findById(1L);
    }

    @Test
    void getBankById_returnsEmpty_whenNotFound() {
        // Arrange
        when(bankRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Bank> result = bankService.getBankById(99L);

        // Assert
        assertTrue(result.isEmpty());
        verify(bankRepository).findById(99L);
    }

    // ============================================================
    // getBankByName()
    // ============================================================
    @Test
    void getBankByName_returnsBank_whenExists() {
        // Arrange
        Bank bank = new Bank("Handelsbanken");
        when(bankRepository.findByName("Handelsbanken")).thenReturn(Optional.of(bank));

        // Act
        Optional<Bank> result = bankService.getBankByName("Handelsbanken");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Handelsbanken", result.get().getName());
        verify(bankRepository).findByName("Handelsbanken");
    }

    @Test
    void getBankByName_returnsEmpty_whenNotFound() {
        // Arrange
        when(bankRepository.findByName("OkändBank")).thenReturn(Optional.empty());

        // Act
        Optional<Bank> result = bankService.getBankByName("OkändBank");

        // Assert
        assertTrue(result.isEmpty());
        verify(bankRepository).findByName("OkändBank");
    }

    // ============================================================
    // getBankByNameIgnoreCase()
    // ============================================================
    @Test
    void getBankByNameIgnoreCase_returnsBank_whenExists() {
        // Arrange
        Bank bank = new Bank("SBAB");
        when(bankRepository.findByNameIgnoreCase("sbab")).thenReturn(Optional.of(bank));

        // Act
        Optional<Bank> result = bankService.getBankByNameIgnoreCase("sbab");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("SBAB", result.get().getName());
        verify(bankRepository).findByNameIgnoreCase("sbab");
    }

    @Test
    void getBankByNameIgnoreCase_returnsEmpty_whenNotFound() {
        // Arrange
        when(bankRepository.findByNameIgnoreCase("fantombank")).thenReturn(Optional.empty());

        // Act
        Optional<Bank> result = bankService.getBankByNameIgnoreCase("fantombank");

        // Assert
        assertTrue(result.isEmpty());
        verify(bankRepository).findByNameIgnoreCase("fantombank");
    }

    // ============================================================
    // saveBank()
    // ============================================================
    @Test
    void saveBank_callsRepositorySave_andReturnsSavedBank() {
        // Arrange
        Bank bankToSave = new Bank("Länsförsäkringar");
        Bank savedBank = new Bank("Länsförsäkringar");
        savedBank.setId(10L);

        when(bankRepository.save(bankToSave)).thenReturn(savedBank);

        // Act
        Bank result = bankService.saveBank(bankToSave);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Länsförsäkringar", result.getName());
        verify(bankRepository).save(bankToSave);
    }

    // ============================================================
    // deleteBank()
    // ============================================================
    @Test
    void deleteBank_callsRepositoryDeleteById() {
        // Act
        bankService.deleteBank(5L);

        // Assert
        verify(bankRepository).deleteById(5L);
        verifyNoMoreInteractions(bankRepository);
    }
}