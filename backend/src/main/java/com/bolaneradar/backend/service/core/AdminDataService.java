package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service för administrativ datahantering under utveckling.
 * Kan användas för att skapa exempeldata eller rensa databasen.
 */
@SuppressWarnings("unused")
@Service
public class AdminDataService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;
    private final RateUpdateLogService rateUpdateLogService;

    public AdminDataService(BankRepository bankRepository,
                            MortgageRateRepository rateRepository,
                            RateUpdateLogService rateUpdateLogService) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
        this.rateUpdateLogService = rateUpdateLogService;
    }

    /**
     * Rensar databasen på all bolåneräntedata och uppdateringsloggar.
     * Används endast i utvecklingssyfte.
     */
    @Transactional
    public void clearDatabase() {
        System.out.println("Rensar databas...");

        // Ta bort räntor först
        rateRepository.deleteAll();

        // Ta bort loggar (om de finns)
        rateUpdateLogService.clearAllLogs();

        System.out.println("Databasen rensad på räntor och loggar.");
    }

    /**
     * Skapar exempeldata för att testa systemet.
     * Banker läggs till om de inte redan finns.
     */
    @Transactional
    public void importExampleData() {
        System.out.println("Importerar exempeldata...");

        // Skapa banker (endast om de inte redan finns)
        Bank swedbank = getOrCreateBank("Swedbank", "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor");
        Bank nordea = getOrCreateBank("Nordea", "https://www.nordea.se/privat/produkter/bolan/bolanerantor");
        Bank handelsbanken = getOrCreateBank("Handelsbanken", "https://www.handelsbanken.se/sv/privat/bolan/bolanerantor");
        Bank seb = getOrCreateBank("SEB", "https://seb.se/privat/bolan/bolanerantor");
        Bank sbab = getOrCreateBank("SBAB", "https://www.sbab.se/1/privat/vara_rantor");
        Bank icabanken = getOrCreateBank("ICA Banken", "https://www.icabanken.se/lana/bolan/bolanerantor/");
        Bank lansforsakringar = getOrCreateBank("Länsförsäkringar Bank", "https://www.lansforsakringar.se/norrbotten/privat/bank/bolan/bolaneranta");
        Bank danskebank = getOrCreateBank("Danske Bank", "https://danskebank.se/privat/produkter/bolan/relaterat/aktuella-bolanerantor");
        Bank Skandia = getOrCreateBank("Skandiabanken", "https://www.skandia.se/lana/bolan/bolanerantor/");
        Bank landshypotekbank = getOrCreateBank("Landshypotek Bank", "https://www.landshypotek.se/lana/bolanerantor/");
        Bank alandsbanken = getOrCreateBank("Ålandsbanken", "https://www.alandsbanken.se/banktjanster/lana-pengar/bolan");
        Bank ikanobank = getOrCreateBank("Ikano Bank", "https://ikanobank.se/bolan/bolanerantor");
    }

    /**
     * Hjälpmetod för att skapa bank endast om den inte redan finns.
     */
    private Bank getOrCreateBank(String name, String website) {
        return bankRepository.findByName(name)
                .orElseGet(() -> {
                    Bank newBank = new Bank(name, website);
                    bankRepository.save(newBank);
                    System.out.println("Skapade bank: " + name);
                    return newBank;
                });
    }

    // Rensa räntor för en specifik bank
    @Transactional
    public String deleteRatesForBank(String bankName) {
        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);

        if (optionalBank.isEmpty()) {
            return "Ingen bank hittades med namn: " + bankName;
        }

        Bank bank = optionalBank.get();
        int countBefore = rateRepository.findByBank(bank).size();
        rateRepository.deleteByBank(bank);

        System.out.println("Rensade " + countBefore + " räntor för " + bank.getName());
        return "Rensade " + countBefore + " räntor för " + bank.getName() + ".";
    }
}
