package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.scraper.BankScraper;
import com.bolaneradar.backend.service.scraper.ScraperService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Tillfällig controller för att testa webbskrapning.
 * Kan tas bort eller döljas när schemaläggning införs.
 */
@RestController
@RequestMapping("/api/scrape")
public class ScraperController {

    private final ScraperService scraperService;
    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;

    public ScraperController(ScraperService scraperService,
                             BankRepository bankRepository,
                             MortgageRateRepository mortgageRateRepository) {
        this.scraperService = scraperService;
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Test: Kör alla aktiva scrapers.
     */
    @GetMapping("/all")
    public String scrapeAll() throws IOException {
        scraperService.scrapeAllBanks();
        return "Skrapning av alla banker körd (kolla loggen för resultat)";
    }

    /** Kör en specifik banks scraper via namn i URL:en */
    @GetMapping("/{bankName}")
    public String scrapeSingleBank(@PathVariable String bankName) throws IOException {
        Bank bank = bankRepository.findByNameIgnoreCase(bankName);

        if (bank == null) {
            return "Ingen bank hittades med namn: " + bankName;
        }

        BankScraper scraper = scraperService.getScraperForBank(bank);
        if (scraper == null) {
            return "Ingen scraper hittades för: " + bank.getName();
        }

        List<MortgageRate> rates = scraper.scrapeRates(bank);
        if (!rates.isEmpty()) {
            mortgageRateRepository.saveAll(rates);
            return rates.size() + " räntor sparade för " + bank.getName();
        }
        return "Inga räntor hittades för " + bank.getName();
    }
}
