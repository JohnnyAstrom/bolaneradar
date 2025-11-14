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
 * Ansvar:
 *  - Hitta korrekt scraper baserat på bankens namn
 *  - Köra skrapning för en eller flera banker
 *  - Undvika dubbletter (särskilt snitträntor)
 *  - Beräkna rateChange när räntor ändras
 *  - Logga uppdateringar
 *  - Skicka mejl vid fel
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
    public void scrapeAllBanks() {
        System.out.println("Startar skrapning av alla banker");

        List<Bank> banks = bankRepository.findAll();
        List<String> failedBanks = new ArrayList<>();

        for (Bank bank : banks) {
            ScraperResult result = scrapeSingleBankResult(bank.getName());
            if (!result.success()) failedBanks.add(bank.getName());
        }

        if (!failedBanks.isEmpty()) {
            emailService.sendErrorNotification(
                    "BolåneRadar – Fel vid scraping",
                    "Följande banker misslyckades:\n- " + String.join("\n- ", failedBanks)
            );
        }

        System.out.println("Skrapning av alla banker slutförd");
    }

    // =============================================================
    // SKRAPA ENDA BANK (kastar exception vid fel)
    // =============================================================
    public String scrapeSingleBank(String bankName) throws Exception {
        ScraperResult result = scrapeSingleBankResult(bankName);

        if (result.success()) {
            return result.importedCount() + " räntor sparade för " + result.bankName();
        }

        throw new Exception(result.error() != null ? result.error() : "Okänt fel vid scraping");
    }

    // =============================================================
    // SKRAPA ENDA BANK (returnerar ScraperResult)
    // =============================================================
    public ScraperResult scrapeSingleBankResult(String bankName) {

        long start = System.currentTimeMillis();

        // 1. Hitta bank
        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);
        if (optionalBank.isEmpty()) {
            long duration = System.currentTimeMillis() - start;
            return new ScraperResult(bankName, 0, false, "Bank saknas: " + bankName, duration);
        }

        Bank bank = optionalBank.get();

        // 2. Hitta scraper
        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            long duration = System.currentTimeMillis() - start;
            return new ScraperResult(bank.getName(), 0, false, "Ingen scraper hittades", duration);
        }

        List<MortgageRate> finalRates = new ArrayList<>();
        boolean success = false;
        String error = null;

        try {
            // 3. Kör scraping
            List<MortgageRate> scrapedRates = scraper.scrapeRates(bank);

            if (scrapedRates == null || scrapedRates.isEmpty()) {
                throw new Exception("Inga räntor hittades för " + bank.getName());
            }

            // 4. Jämför med databasen
            for (MortgageRate newRate : scrapedRates) {

                List<MortgageRate> previous =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                newRate.getBank(),
                                newRate.getTerm(),
                                newRate.getRateType()
                        );

                if (!previous.isEmpty()) {

                    MortgageRate latest = previous.get(0);

                    // =====================================================
                    // 4A. FÖRHINDRA DUBLETTER FÖR SNITTRÄNTOR
                    // =====================================================
                    if (newRate.getRateType() == RateType.AVERAGERATE) {

                        boolean exists = mortgageRateRepository
                                .existsByBankAndTermAndRateTypeAndEffectiveDate(
                                        newRate.getBank(),
                                        newRate.getTerm(),
                                        RateType.AVERAGERATE,
                                        newRate.getEffectiveDate()
                                );

                        if (exists) {
                            System.out.println(
                                    "Hoppar över snittränta som redan finns: "
                                            + bank.getName()
                                            + " (" + newRate.getTerm() + ")"
                                            + " " + newRate.getEffectiveDate()
                            );
                            continue;
                        }
                    }

                    // 4B. Om datumet är nyare och räntan ändrat → sätt rateChange
                    if (newRate.getEffectiveDate().isAfter(latest.getEffectiveDate())
                            && newRate.getRatePercent().compareTo(latest.getRatePercent()) != 0) {

                        newRate.setRateChange(
                                newRate.getRatePercent().subtract(latest.getRatePercent())
                        );
                        newRate.setLastChangedDate(newRate.getEffectiveDate());
                    }
                }

                finalRates.add(newRate);
            }

            // 5. Spara nya rader
            if (!finalRates.isEmpty()) {
                mortgageRateRepository.saveAll(finalRates);
            }

            success = true;

        } catch (Exception e) {
            error = e.getMessage();
        }

        // 6. Logga resultat
        long duration = System.currentTimeMillis() - start;

        rateUpdateLogService.logUpdate(
                bank,
                "ScraperService",
                finalRates.size(),
                success,
                error,
                duration
        );

        return new ScraperResult(
                bank.getName(),
                finalRates.size(),
                success,
                error,
                duration
        );
    }

    // =============================================================
    // Matcha rätt scraper
    // =============================================================
    public BankScraper getScraperForBank(Bank bank) {
        String bankNameNorm = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> {
                    String simple = normalize(s.getClass().getSimpleName());
                    String display = normalize(s.toString());
                    return simple.contains(bankNameNorm)
                            || display.contains(bankNameNorm)
                            || bankNameNorm.contains(simple);
                })
                .findFirst()
                .orElse(null);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}