package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.model.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service för administrativ datahantering under utveckling.
 * Kan användas för att skapa exempeldata eller rensa databasen.
 */
@Service
public class AdminDataService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;
    private final RateUpdateService rateUpdateService;

    public AdminDataService(BankRepository bankRepository,
                            MortgageRateRepository rateRepository,
                            RateUpdateService rateUpdateService) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
        this.rateUpdateService = rateUpdateService;
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
        rateUpdateService.clearAllLogs();

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


        // Skapa exempelräntor
        List<MortgageRate> rates = List.of(
                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(1)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(1)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.60"), LocalDate.now().minusDays(1)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.40"), LocalDate.now().minusDays(1)),
                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.05"), LocalDate.now().minusDays(2)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(2)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.60"), LocalDate.now().minusDays(2)),
                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.40"), LocalDate.now().minusDays(2)),

                new MortgageRate(nordea, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.00"), LocalDate.now().minusDays(1)),
                new MortgageRate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("4.00"), LocalDate.now().minusDays(1)),
                new MortgageRate(nordea, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now().minusDays(1)),
                new MortgageRate(nordea, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.65"), LocalDate.now().minusDays(1)),
                new MortgageRate(nordea, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.20"), LocalDate.now().minusDays(2)),
                new MortgageRate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("4.10"), LocalDate.now().minusDays(2)),
                new MortgageRate(nordea, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now().minusDays(2)),
                new MortgageRate(nordea, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.65"), LocalDate.now().minusDays(2))
        );

        rateRepository.saveAll(rates);
        rateUpdateService.logUpdate(swedbank, "ExampleData", rates.size());

        System.out.println("Exempeldata importerad!");
    }

    /**
     * Hjälpmetod för att skapa bank endast om den inte redan finns.
     */
    private Bank getOrCreateBank(String name, String website) {
        Bank bank = bankRepository.findByName(name);
        if (bank == null) {
            bank = new Bank(name, website);
            bankRepository.save(bank);
            System.out.println("Skapade bank: " + name);
        } else {
            System.out.println("Bank finns redan: " + name);
        }
        return bank;
    }

    // Rensa räntor för en specifik bank
    @Transactional
    public String deleteRatesForBank(String bankName) {
        Bank bank = bankRepository.findByNameIgnoreCase(bankName);
        if (bank == null) {
            return "Ingen bank hittades med namn: " + bankName;
        }

        int countBefore = rateRepository.findByBank(bank).size();
        rateRepository.deleteByBank(bank);

        System.out.println("Rensade " + countBefore + " räntor för " + bank.getName());
        return "Rensade " + countBefore + " räntor för " + bank.getName() + ".";
    }
}
