package com.bolaneradar.backend.service.integration.scraper;

import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.integration.EmailService;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Koordinator som hanterar anrop till olika bank-scrapers.
 * Ansvarar för att hämta banker, köra respektive scraper och spara resultaten i databasen.
 */
@Service
public class ScraperService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;
    private final List<BankScraper> scrapers;
    private final RateUpdateLogService rateUpdateLogService;
    private final EmailService emailService;

    public ScraperService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository,
            List<BankScraper> scrapers,
            RateUpdateLogService rateUpdateLogService,
            EmailService emailService
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
        this.scrapers = scrapers;
        this.rateUpdateLogService = rateUpdateLogService;
        this.emailService = emailService;
    }

    /**
     * Kör webbskrapning för alla banker med tillgänglig scraper.
     * En förenklad, lättläst loop som använder typat resultat.
     */
    public void scrapeAllBanks() {
        System.out.println("Startar skrapning av alla banker");

        List<Bank> banks = bankRepository.findAll();
        List<String> failedBanks = new ArrayList<>();

        for (Bank bank : banks) {
            ScrapeResult result = scrapeSingleBankResult(bank.getName());

            if (!result.success()) {
                failedBanks.add(bank.getName());
            }
        }

        if (!failedBanks.isEmpty()) {
            String message = "Följande banker misslyckades:\n- " + String.join("\n- ", failedBanks)
                    + "\n\nKontrollera loggarna i /api/rates/updates för mer information.";
            emailService.sendErrorNotification("BolåneRadar – Fel vid scraping", message);
            System.err.println(failedBanks.size() + " banker misslyckades. E-postnotifiering skickad.");
        } else {
            System.out.println("Alla banker skrapades utan fel");
        }

        System.out.println("Skrapning av alla banker slutförd");
    }

    /**
     * Bakåtkompatibel metod för controllers som vill ha text.
     * Returnerar ett läsbart meddelande vid success, kastar Exception vid fel.
     */
    public String scrapeSingleBank(String bankName) throws Exception {
        ScrapeResult r = scrapeSingleBankResult(bankName);
        if (r.success()) {
            return r.importedCount() + " räntor sparade för " + r.bankName();
        }
        throw new Exception(r.error() != null ? r.error() : "Okänt fel vid scraping av " + bankName);
    }

    /**
     * Typat huvudflöde för en specifik bank.
     * Kastar inte Exception, utan kapslar utfallet i ScrapeResult.
     * Loggar alltid resultat i RateUpdateLogService.
     */
    public ScrapeResult scrapeSingleBankResult(String bankName) {
        long startTime = System.currentTimeMillis();

        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);
        if (optionalBank.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("Ingen bank hittades med namn: " + bankName);
            // Ingen logUpdate här eftersom vi saknar Bank-entitet
            return new ScrapeResult(bankName, 0, false, "Ingen bank hittades med namn: " + bankName, duration);
        }

        Bank bank = optionalBank.get();

        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("Ingen scraper hittades för: " + bank.getName());
            // Logga misslyckandet på banken
            rateUpdateLogService.logUpdate(bank, "ScraperService", 0, false,
                    "Ingen scraper hittades för banken", duration);
            return new ScrapeResult(bank.getName(), 0, false,
                    "Ingen scraper hittades för " + bank.getName(), duration);
        }

        System.out.println("Startar skrapning för " + bank.getName() + "...");

        int importedCount = 0;
        boolean success = false;
        String errorMessage = null;

        try {
            List<MortgageRate> scrapedRates = scraper.scrapeRates(bank);

            if (scrapedRates == null || scrapedRates.isEmpty()) {
                throw new Exception("Inga räntor hittades för " + bank.getName());
            }

            List<MortgageRate> finalRatesToSave = new ArrayList<>();

            for (MortgageRate newRate : scrapedRates) {
                List<MortgageRate> previousRates =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                newRate.getBank(),
                                newRate.getTerm(),
                                newRate.getRateType()
                        );

                if (!previousRates.isEmpty()) {
                    MortgageRate latest = previousRates.get(0);

                    // Snittränta: hoppa över dubbletter (samma datum och värde)
                    if (newRate.getRateType() == RateType.AVERAGERATE) {
                        boolean sameDate = newRate.getEffectiveDate().equals(latest.getEffectiveDate());
                        boolean sameRate = newRate.getRatePercent().compareTo(latest.getRatePercent()) == 0;

                        if (sameDate && sameRate) {
                            System.out.println("Hoppar över oförändrad snittränta för "
                                    + bank.getName() + " (" + newRate.getTerm() + ")");
                            continue;
                        }
                    }

                    // Sätt rateChange och lastChangedDate om datum är nytt och värdet ändrat
                    if (newRate.getEffectiveDate().isAfter(latest.getEffectiveDate())
                            && newRate.getRatePercent().compareTo(latest.getRatePercent()) != 0) {
                        newRate.setRateChange(newRate.getRatePercent().subtract(latest.getRatePercent()));
                        newRate.setLastChangedDate(newRate.getEffectiveDate());
                    }
                }

                finalRatesToSave.add(newRate);
            }

            if (!finalRatesToSave.isEmpty()) {
                mortgageRateRepository.saveAll(finalRatesToSave);
            }

            importedCount = finalRatesToSave.size();
            success = true;
            System.out.println(importedCount + " räntor sparade för " + bank.getName());

        } catch (Exception e) {
            errorMessage = e.getMessage();
            System.err.println("Fel vid skrapning av " + bank.getName() + ": " + errorMessage);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            rateUpdateLogService.logUpdate(bank, "ScraperService", importedCount, success, errorMessage, duration);
            System.out.println("Loggat resultat för " + bank.getName()
                    + " (success=" + success + ", time=" + duration + "ms)");
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        return new ScrapeResult(bank.getName(), importedCount, success, errorMessage, totalDuration);
    }

    /**
     * Hjälpare som returnerar endast antal importerade räntor.
     */
    public int scrapeSingleBankCount(String bankName) {
        return scrapeSingleBankResult(bankName).importedCount();
    }

    /**
     * Hittar rätt scraper baserat på bankens namn.
     * Matchar t.ex. "Swedbank" mot "SwedbankScraper".
     */
    public BankScraper getScraperForBank(Bank bank) {
        String bankNameNorm = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> {
                    String scraperNameNorm = normalize(s.getClass().getSimpleName());
                    return scraperNameNorm.contains(bankNameNorm) || bankNameNorm.contains(scraperNameNorm);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Normalisering av namn: tar bort mellanslag, ersätter å/ä/ö och gör gemener.
     */
    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}