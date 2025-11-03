package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.repository.BankRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service-lager för hantering av banker.
 * Innehåller logiken mellan controller och repository.
 */
@Service
public class BankService {

    private final BankRepository bankRepository;

    // Konstruktor-injektion vilket betyder att Spring skickar in vårt repository automatiskt
    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    /**
     * Hämta alla banker i databasen.
     */
    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }

    /**
     * Hämta en bank baserat på dess ID.
     */
    public Optional<Bank> getBankById(Long id) {
        return bankRepository.findById(id);
    }

    /**
     * Hämta en bank baserat på dess namn.
     */
    public Optional<Bank> getBankByName(String name) {
        return bankRepository.findByName(name);
    }

    /**
     * Skapa eller uppdatera bank.
     * /Spring avgör auitomatiskt om det är insert eller update)
     */
    public Bank saveBank(Bank bank) {
        return bankRepository.save(bank);
    }

    /**
     * Ta bort en bank via ID.
     */
    public void deleteBank(Long id) {
        bankRepository.deleteById(id);
    }

}
