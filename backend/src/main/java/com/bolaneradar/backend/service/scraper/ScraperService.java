package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Koordinator som hanterar anrop till olika bank-scrapers.
 * Ansvarar för att hämta alla banker, köra deras respektive scraper och spara resultaten.
 */
@Service
public class ScraperService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;
    private final List<BankScraper> scrapers;

    public ScraperService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository,
            List<BankScraper> scrapers
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
        this.scrapers = scrapers;
    }

    /**
     * Kör webbskrapning för alla banker som har en tillgänglig scraper.
     * Resultaten sparas i databasen.
     */

    public void scrapeAllBanks() throws IOException {
        System.out.println("Aktiva scrapers:");
        scrapers.forEach(s -> System.out.println(" - " + s.getClass().getSimpleName()));
        System.out.println("-------------------------------------------");

        List<Bank> banks = bankRepository.findAll();
        List<MortgageRate> allRates = new ArrayList<>();

        for (Bank bank : banks) {
            BankScraper matchingScraper = getScraperForBank(bank);
            if (matchingScraper != null) {
                System.out.println("Hämtar räntor för " + bank.getName() + "...");
                List<MortgageRate> rates = matchingScraper.scrapeRates(bank);
                allRates.addAll(rates);
            } else {
                System.out.println("Ingen scraper hittades för: " + bank.getName());
            }
        }

        if (!allRates.isEmpty()) {
            mortgageRateRepository.saveAll(allRates);
            System.out.println("Sparade " + allRates.size() + " räntor i databasen.");
        } else {
            System.out.println("Inga räntor att spara.");
        }
    }

    /**
     * Hittar rätt scraper baserat på bankens namn.
     * Matchar tex 'Swedbank' mot 'SwedbankScraper'.
     */
    public BankScraper getScraperForBank(Bank bank) {
        String bankName = bank.getName().toLowerCase();
        return scrapers.stream()
                .filter(s -> s.getClass().getSimpleName().toLowerCase().contains(bankName))
                .findFirst()
                .orElse(null);
    }
}
