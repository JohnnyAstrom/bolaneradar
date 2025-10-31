package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.RateUpdateService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Koordinator som hanterar anrop till olika bank-scrapers.
 * Ansvarar för att hämta banker, köra respektive scraper
 * och spara resultaten i databasen.
 */
@Service
public class ScraperService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;
    private final List<BankScraper> scrapers;
    private final RateUpdateService rateUpdateService;

    public ScraperService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository,
            List<BankScraper> scrapers,
            RateUpdateService rateUpdateService
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
        this.scrapers = scrapers;
        this.rateUpdateService = rateUpdateService;
    }

    /**
     * Kör webbskrapning för alla banker som har en tillgänglig scraper.
     * Resultaten sparas och loggas per bank.
     */
    public void scrapeAllBanks() throws IOException {
        System.out.println("=== Startar skrapning av alla banker ===");
        List<Bank> banks = bankRepository.findAll();

        for (Bank bank : banks) {
            try {
                String result = scrapeSingleBank(bank.getName());
                System.out.println(result);
            } catch (IOException e) {
                System.err.println("Fel vid skrapning av " + bank.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("=== Skrapning klar ===");
    }

    /**
     * Kör skrapning för en specifik bank via dess namn.
     *
     * @param bankName Namnet på banken (t.ex. "Swedbank")
     * @return Textmeddelande med resultatet (t.ex. "5 räntor sparade för Swedbank")
     * @throws IOException vid nätverks- eller scrapingfel
     */
    public String scrapeSingleBank(String bankName) throws IOException {
        Bank bank = bankRepository.findByNameIgnoreCase(bankName);
        if (bank == null) {
            return "Ingen bank hittades med namn: " + bankName;
        }

        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            return "Ingen scraper hittades för: " + bank.getName();
        }

        System.out.println("▶️ Startar skrapning för " + bank.getName() + "...");
        List<MortgageRate> rates = scraper.scrapeRates(bank);

        if (rates.isEmpty()) {
            return "Inga räntor hittades för " + bank.getName();
        }

        // Spara nya räntor i databasen
        mortgageRateRepository.saveAll(rates);

        // Logga uppdateringen
        rateUpdateService.logUpdate(bank, "ScraperService", rates.size());

        return rates.size() + " räntor sparade för " + bank.getName();
    }

    /**
     * Hittar rätt scraper baserat på bankens namn.
     * Matchar t.ex. 'Swedbank' mot 'SwedbankScraper'.
     * Normaliserar även svenska tecken (å, ä, ö).
     */
    public BankScraper getScraperForBank(Bank bank) {
        String bankName = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> normalize(s.getClass().getSimpleName()).contains(bankName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Hjälpmetod som normaliserar namn:
     * - Tar bort mellanslag
     * - Ersätter å, ä, ö med a/o
     * - Gör allt till gemener
     */
    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}