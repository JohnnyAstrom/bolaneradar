package com.bolaneradar.backend.service.integration.scraper.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.integration.EmailService;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hanterar skrapning av räntedata från olika banker.
 *
 * Detta är kärnan i hela integrationsflödet.
 *
 * Ansvar:
 *  - Hitta korrekt scraper baserat på bankens namn
 *  - Köra skrapning för en eller flera banker
 *  - Jämföra nya räntor med befintliga
 *  - Undvika dubbletter (särskilt för snitträntor)
 *  - Registrera uppdateringshistorik (RateUpdateLog)
 *  - Skicka e-post om fel uppstår
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

    // =============================================================
    // SKRAPA ALLA BANKER
    // =============================================================
    /**
     * Kör scraping av alla banker som finns i databasen.
     * Misslyckade banker samlas i en lista och skickas via e-post.
     */
    public void scrapeAllBanks() {
        System.out.println("Startar skrapning av alla banker");

        List<Bank> banks = bankRepository.findAll();
        List<String> failedBanks = new ArrayList<>();

        for (Bank bank : banks) {

            // Här använder vi scrapeSingleBankResult så att vi alltid får tillbaka
            // ett objekt med både success/error/antal importerade rader.
            ScraperResult result = scrapeSingleBankResult(bank.getName());

            if (!result.success()) {
                failedBanks.add(bank.getName());
            }
        }

        // Skicka e-post vid fel
        if (!failedBanks.isEmpty()) {
            String message = "Följande banker misslyckades:\n- "
                    + String.join("\n- ", failedBanks)
                    + "\n\nKontrollera loggarna i /api/rates/updates för mer information.";

            emailService.sendErrorNotification("BolåneRadar – Fel vid scraping", message);
            System.err.println(failedBanks.size() + " banker misslyckades. E-postnotifiering skickad.");
        } else {
            System.out.println("Alla banker skrapades utan fel");
        }

        System.out.println("Skrapning av alla banker slutförd");
    }

    // =============================================================
    // SKRAPA EN ENDA BANK (kastar exception vid fel)
    // =============================================================
    /**
     * Wrapper-metod som används av controller.
     * Returnerar text vid lyckad skrapning, kastar exception vid fel.
     */
    public String scrapeSingleBank(String bankName) throws Exception {
        ScraperResult result = scrapeSingleBankResult(bankName);

        if (result.success()) {
            return result.importedCount() + " räntor sparade för " + result.bankName();
        }

        throw new Exception(result.error() != null
                ? result.error()
                : "Okänt fel vid scraping av " + bankName);
    }

    // =============================================================
    // SKRAPA EN BANK (returnerar ScraperResult)
    // =============================================================
    /**
     * Detta är den viktiga funktionen som driver allt:
     *
     * 1. Hittar bank i databasen
     * 2. Hittar rätt scraper (via klassnamn)
     * 3. Kör scraping
     * 4. Undviker dubletter av snitträntor
     * 5. Beräknar rateChange om räntan ändrats
     * 6. Sparar nya rader
     * 7. Loggar resultatet
     */
    public ScraperResult scrapeSingleBankResult(String bankName) {

        long startTime = System.currentTimeMillis();

        // ---------------------------------------------------------
        // 1. Hitta banken
        // ---------------------------------------------------------
        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);
        if (optionalBank.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;

            System.err.println("Ingen bank hittades med namn: " + bankName);

            return new ScraperResult(
                    bankName,
                    0,
                    false,
                    "Ingen bank hittades med namn: " + bankName,
                    duration
            );
        }

        Bank bank = optionalBank.get();

        // ---------------------------------------------------------
        // 2. Hitta rätt scraper baserat på namnet (enkel matchning)
        // ---------------------------------------------------------
        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            long duration = System.currentTimeMillis() - startTime;

            System.err.println("Ingen scraper hittades för: " + bank.getName());

            return new ScraperResult(
                    bank.getName(),
                    0,
                    false,
                    "Ingen scraper hittades för " + bank.getName(),
                    duration
            );
        }

        System.out.println("Startar skrapning för " + bank.getName() + "...");

        List<MortgageRate> finalRatesToSave = new ArrayList<>();
        boolean success = false;
        String errorMessage = null;

        try {
            // -----------------------------------------------------
            // 3. Kör scraping för banken
            // -----------------------------------------------------
            List<MortgageRate> scrapedRates = scraper.scrapeRates(bank);

            if (scrapedRates == null || scrapedRates.isEmpty()) {
                throw new Exception("Inga räntor hittades för " + bank.getName());
            }

            // -----------------------------------------------------
            // 4. Jämför med senaste värde i databasen
            //    Hanterar:
            //      - undvika dubletter av snittränta
            //      - beräkna rateChange när räntor ändras
            // -----------------------------------------------------
            for (MortgageRate newRate : scrapedRates) {

                List<MortgageRate> previousRates =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                newRate.getBank(),
                                newRate.getTerm(),
                                newRate.getRateType()
                        );

                if (!previousRates.isEmpty()) {

                    MortgageRate latest = previousRates.get(0);

                    // 4A. Undvik dubletter av snittränta
                    if (newRate.getRateType() == RateType.AVERAGERATE) {

                        boolean sameDate = newRate.getEffectiveDate().equals(latest.getEffectiveDate());
                        boolean sameRate = newRate.getRatePercent().compareTo(latest.getRatePercent()) == 0;

                        if (sameDate && sameRate) {
                            System.out.println(
                                    "Hoppar över oförändrad snittränta för "
                                            + bank.getName() + " (" + newRate.getTerm() + ")"
                            );
                            continue;
                        }
                    }

                    // 4B. Om datum är nyare och räntan ändrats → sätt rateChange
                    if (newRate.getEffectiveDate().isAfter(latest.getEffectiveDate())
                            && newRate.getRatePercent().compareTo(latest.getRatePercent()) != 0) {

                        newRate.setRateChange(newRate.getRatePercent().subtract(latest.getRatePercent()));
                        newRate.setLastChangedDate(newRate.getEffectiveDate());
                    }
                }

                // Godkänd för sparning
                finalRatesToSave.add(newRate);
            }

            // -----------------------------------------------------
            // 5. Spara alla nya rader i databasen
            // -----------------------------------------------------
            if (!finalRatesToSave.isEmpty()) {
                mortgageRateRepository.saveAll(finalRatesToSave);
            }

            success = true;

        } catch (Exception e) {

            // -----------------------------------------------------
            // 6. Fångar alla fel i scrapingprocessen
            // -----------------------------------------------------
            errorMessage = e.getMessage();
            System.err.println("Fel vid skrapning av " + bank.getName() + ": " + errorMessage);
        }

        // ---------------------------------------------------------
        // 7. Logga alla uppdateringar
        // ---------------------------------------------------------
        long duration = System.currentTimeMillis() - startTime;

        rateUpdateLogService.logUpdate(
                bank,
                "ScraperService",
                finalRatesToSave.size(), // antal sparade rader
                success,
                errorMessage,
                duration
        );

        return new ScraperResult(
                bank.getName(),
                finalRatesToSave.size(),
                success,
                errorMessage,
                duration
        );
    }

    // =============================================================
    // Hitta rätt scraper baserat på bankens namn
    // =============================================================
    /**
     * Matchar bankens namn mot scraper-klassens namn.
     * Exempel:
     *  - "Swedbank" → SwedbankScraper
     *  - "SBAB" → SBABScraper
     *
     * Normaliserar text genom att ta bort mellanslag och svenska tecken.
     */
    public BankScraper getScraperForBank(Bank bank) {
        String bankNameNorm = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> {
                    String className = normalize(s.getClass().getSimpleName());
                    String displayName = normalize(s.toString());

                    return className.contains(bankNameNorm)
                            || displayName.contains(bankNameNorm)
                            || bankNameNorm.contains(className)
                            || bankNameNorm.contains(displayName);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * En enkel normaliseringsfunktion:
     *  - gör små bokstäver
     *  - tar bort mellanslag
     *  - ersätter å/ä → a, ö → o
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