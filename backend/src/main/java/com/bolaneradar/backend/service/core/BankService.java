package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.repository.BankRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * BANK SERVICE
 * ================================================================
 * <p>
 * Centralt service-lager för hantering av banker.
 * Utgör gränssnittet mellan controller-lagret och BankRepository.
 * <p>
 * Ansvar:
 * - Läsa och skriva Bank-entiteter
 * - Kapsla repository-anrop relaterade till banker
 * <p>
 * Designprinciper:
 * - Innehåller endast enkel affärslogik
 * - Returnerar alltid entiteter (ingen DTO-mappning)
 * - Återanvänds av både client- och admin-services
 * ================================================================
 */
@Service
public class BankService {

    private final BankRepository bankRepository;

    /**
     * Konstruktorinjektion.
     * Spring skapar automatiskt en instans av BankRepository och injicerar den här.
     */
    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    // ============================================================
    // ===================      READ (GET)      ===================
    // ============================================================

    /**
     * Hämtar alla banker i databasen.
     *
     * @return lista med alla banker.
     */
    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }

    /**
     * Hämtar en bank baserat på dess ID.
     *
     * @param id bankens ID
     * @return en Optional med banken om den finns
     */
    public Optional<Bank> getBankById(Long id) {
        return bankRepository.findById(id);
    }

    /**
     * Hämtar en bank baserat på dess namn (skiftlägeskänsligt).
     *
     * @param name bankens namn
     * @return en Optional med banken om den finns
     */
    public Optional<Bank> getBankByName(String name) {
        return bankRepository.findByName(name);
    }

    /**
     * Hämtar en bank baserat på dess namn utan hänsyn till versaler/gemener.
     *
     * @param name bankens namn
     * @return en Optional med banken om den finns
     */
    public Optional<Bank> getBankByNameIgnoreCase(String name) {
        return bankRepository.findByNameIgnoreCase(name);
    }

    // ============================================================
    // ===================     WRITE (SAVE)     ===================
    // ============================================================

    /**
     * Sparar en ny bank eller uppdaterar en befintlig.
     * Spring Data JPA avgör automatiskt om det ska göras en insert eller update.
     *
     * @param bank bankobjekt som ska sparas
     * @return den sparade banken
     */
    public Bank saveBank(Bank bank) {
        return bankRepository.save(bank);
    }

    // ============================================================
    // ===================     DELETE (REMOVE)   ===================
    // ============================================================

    /**
     * Tar bort en bank baserat på dess ID.
     *
     * @param id bankens ID
     */
    public void deleteBank(Long id) {
        bankRepository.deleteById(id);
    }
}