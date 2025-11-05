package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.EmailService;
import com.bolaneradar.backend.service.RateUpdateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final EmailService emailService; // Nytt fält för e-postnotifieringar

    public ScraperService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository,
            List<BankScraper> scrapers,
            RateUpdateService rateUpdateService,
            EmailService emailService // injiceras automatiskt av Spring
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
        this.scrapers = scrapers;
        this.rateUpdateService = rateUpdateService;
        this.emailService = emailService; // tilldelas till fältet
    }

    /**
     * Kör webbskrapning för alla banker som har en tillgänglig scraper.
     * Resultaten sparas och loggas per bank. Om en eller flera banker misslyckas
     * skickas även en e-postnotifiering via EmailService.
     */
    public void scrapeAllBanks() {
        System.out.println("=== Startar skrapning av alla banker ===");
        List<Bank> banks = bankRepository.findAll();

        int failedCount = 0;                      // Räknare för misslyckade banker
        List<String> failedBanks = new ArrayList<>(); // Lista över banker som misslyckades

        for (Bank bank : banks) {
            long startTime = System.currentTimeMillis();
            int importedCount = 0;
            boolean success = false;
            String errorMessage = null;

            try {
                // Försök köra scraping för en bank
                String result = scrapeSingleBank(bank.getName());
                System.out.println(result);

                // Bedöm om det lyckades utifrån svaret
                success = !result.startsWith("Fel");

                // Extrahera antalet importerade räntor vid lyckad scraping
                if (success && result.matches("\\d+.*")) {
                    importedCount = Integer.parseInt(result.split(" ")[0]);
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
                success = false;
                failedCount++;
                failedBanks.add(bank.getName());
                System.err.println("Fel vid skrapning av " + bank.getName() + ": " + errorMessage);

            } finally {
                long duration = System.currentTimeMillis() - startTime;

                // Logga resultatet i databasen (lyckad eller ej)
                rateUpdateService.logUpdate(
                        bank,
                        "ScraperService",
                        importedCount,
                        success,
                        errorMessage,
                        duration
                );

                System.out.println("Loggat resultat för " + bank.getName()
                        + " (success=" + success + ", time=" + duration + "ms)");
            }
        }

        System.out.println("=== Skrapning klar ===");

        // Om någon bank misslyckades – skicka ett e-postmeddelande
        if (failedCount > 0) {
            String message = "Antal misslyckade banker: " + failedCount + "\n\n"
                    + "Banker med fel:\n- " + String.join("\n- ", failedBanks)
                    + "\n\nKontrollera loggarna i /api/rates/updates för mer information.";

            emailService.sendErrorNotification(
                    "BolåneRadar – Fel vid scraping",
                    message
            );

            System.err.println(failedCount + " banker misslyckades. E-postnotifiering skickad.");
        } else {
            System.out.println("Alla banker skrapades utan fel!");
        }
    }
    /**
     * Kör skrapning för en specifik bank via dess namn.
     *
     * @param bankName Namnet på banken (t.ex. "Swedbank")
     * @return Textmeddelande med resultatet (t.ex. "5 räntor sparade för Swedbank")
     */
    public String scrapeSingleBank(String bankName) throws Exception {
        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);

        if (optionalBank.isEmpty()) {
            throw new Exception("Ingen bank hittades med namn: " + bankName);
        }

        Bank bank = optionalBank.get();
        BankScraper scraper = getScraperForBank(bank);

        if (scraper == null) {
            throw new Exception("Ingen scraper hittades för: " + bank.getName());
        }

        System.out.println("Startar skrapning för " + bank.getName() + "...");

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        int importedCount = 0;

        try {
            List<MortgageRate> rates = scraper.scrapeRates(bank);

            if (rates.isEmpty()) {
                throw new Exception("Inga räntor hittades för " + bank.getName());
            }

            // Skapa lista för slutgiltiga räntor som verkligen ska sparas
            List<MortgageRate> finalRatesToSave = new ArrayList<>();

            // Kontrollera varje ny ränta
            for (MortgageRate newRate : rates) {
                List<MortgageRate> previousRates =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                newRate.getBank(),
                                newRate.getTerm(),
                                newRate.getRateType()
                        );

                if (!previousRates.isEmpty()) {
                    MortgageRate latest = previousRates.get(0);

                    // 💡 Om det är snittränta: hoppa över dubbletter (samma månad + samma värde)
                    if (newRate.getRateType() == RateType.AVERAGERATE) {
                        boolean sameMonth = newRate.getEffectiveDate().equals(latest.getEffectiveDate());
                        boolean sameRate = newRate.getRatePercent().compareTo(latest.getRatePercent()) == 0;

                        if (sameMonth && sameRate) {
                            System.out.println("⏸ Hoppar över oförändrad snittränta för "
                                    + bank.getName() + " (" + newRate.getTerm() + ")");
                            continue; // hoppa över – ingen förändring
                        }
                    }

                    // Beräkna förändring (för både list- och snitträntor)
                    if (newRate.getEffectiveDate().isAfter(latest.getEffectiveDate())) {
                        if (newRate.getRatePercent().compareTo(latest.getRatePercent()) != 0) {
                            newRate.setRateChange(
                                    newRate.getRatePercent().subtract(latest.getRatePercent())
                            );
                            newRate.setLastChangedDate(newRate.getEffectiveDate());
                        }
                    }
                }

                // Lägg till i listan för sparning
                finalRatesToSave.add(newRate);
            }

            // Spara bara de nya/ändrade räntorna
            if (!finalRatesToSave.isEmpty()) {
                mortgageRateRepository.saveAll(finalRatesToSave);
            }

            importedCount = finalRatesToSave.size();
            success = true;

            System.out.println(importedCount + " räntor sparade för " + bank.getName());

        } catch (Exception e) {
            errorMessage = e.getMessage();
            System.err.println("Fel vid skrapning av " + bank.getName() + ": " + e.getMessage());
            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            rateUpdateService.logUpdate(bank, "ScraperService", importedCount, success, errorMessage, duration);
            System.out.println("Loggat resultat för " + bank.getName()
                    + " (success=" + success + ", time=" + duration + "ms)");
        }

        return importedCount + " räntor sparade för " + bank.getName();
    }


    /**
     * Hittar rätt scraper baserat på bankens namn.
     * Matchar t.ex. "Swedbank" mot "SwedbankScraper".
     * Normaliserar även svenska tecken (å, ä, ö) och tar bort mellanslag.
     * Gör matchningen flexibel åt båda håll (banknamn <-> scraper-namn).
     */
    public BankScraper getScraperForBank(Bank bank) {
        String bankNameNorm = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> {
                    String scraperNameNorm = normalize(s.getClass().getSimpleName());

                    // Gör matchningen flexibel åt båda håll
                    return scraperNameNorm.contains(bankNameNorm) || bankNameNorm.contains(scraperNameNorm);
                })
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
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}