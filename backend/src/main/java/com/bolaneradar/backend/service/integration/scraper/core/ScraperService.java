package com.bolaneradar.backend.service.integration.scraper.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.admin.RateUpdateLogService;
import com.bolaneradar.backend.service.integration.EmailService;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScraperService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;
    private final List<BankScraper> scrapers;
    private final RateUpdateLogService rateUpdateLogService;
    private final Optional<EmailService> emailService;

    public ScraperService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository,
            List<BankScraper> scrapers,
            RateUpdateLogService rateUpdateLogService,
            Optional<EmailService> emailService
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
        this.scrapers = scrapers;
        this.rateUpdateLogService = rateUpdateLogService;
        this.emailService = emailService;
    }

    // ==========================================================
    // PUBLIC API (Controller uses these)
    // ==========================================================

    public String scrapeSingleBank(String bankName) throws Exception {
        ScraperResult result = runScrapeForBank(bankName);

        if (!result.success()) {
            throw new Exception(result.error());
        }

        return result.importedCount() + " räntor sparade för " + result.bankName();
    }

    public ScrapeBatchResult scrapeAllBanks() {
        int success = 0;
        int failure = 0;

        List<String> failedBanks = new ArrayList<>();

        // 1. Hämta banker och sortera så att SEB körs sist
        List<Bank> banks = new ArrayList<>(bankRepository.findAll());

        banks.sort((a, b) -> {
            boolean aIsSeb = isSeb(a);
            boolean bIsSeb = isSeb(b);

            // false < true → SEB hamnar sist
            return Boolean.compare(aIsSeb, bIsSeb);
        });

        // 2. Kör scraping i vald ordning
        for (Bank bank : banks) {
            ScraperResult r = runScrapeForBank(bank.getName());

            if (r.success()) {
                success++;
            } else {
                failure++;
                failedBanks.add(bank.getName());
            }
        }

        // 3. Mail vid fel
        if (!failedBanks.isEmpty()) {
            emailService.ifPresent(mail ->
                    mail.sendErrorNotification(
                            "BolåneRadar – Fel vid scraping",
                            "Följande banker misslyckades:\n- " +
                                    String.join("\n- ", failedBanks)
                    )
            );
        }

        return new ScrapeBatchResult(success, failure);
    }

    private boolean isSeb(Bank bank) {
        return bank.getName() != null &&
                bank.getName().toLowerCase().contains("seb");
    }

    // ==========================================================
    // CORE LOGIC – ONLY place that scraping happens
    // ==========================================================

    public ScraperResult runScrapeForBank(String bankName) {

        long start = System.currentTimeMillis();
        int savedCount = 0;
        String error = null;

        // 1. Fetch Bank
        Optional<Bank> opt = bankRepository.findByNameIgnoreCase(bankName);
        if (opt.isEmpty()) {
            long dur = System.currentTimeMillis() - start;
            return new ScraperResult(bankName, 0, false,
                    "Ingen bank hittades med namn: " + bankName, dur);
        }
        Bank bank = opt.get();

        // 2. Find matching scraper
        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            long dur = System.currentTimeMillis() - start;
            rateUpdateLogService.logUpdate(bank, "ScraperService",
                    0, false, "Ingen scraper hittades", dur);
            return new ScraperResult(bank.getName(), 0, false,
                    "Ingen scraper hittades", dur);
        }

        List<MortgageRate> finalRates = new ArrayList<>();

        try {
            // 3. Scrape
            List<MortgageRate> scraped = scraper.scrapeRates(bank);
            if (scraped == null || scraped.isEmpty()) {
                error = "Scraper returnerade 0 räntor";
            }

// 4. Filter logic (duplicates + rateChange + lastChanged)
            for (MortgageRate newRate : scraped) {

                List<MortgageRate> previous =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                newRate.getBank(),
                                newRate.getTerm(),
                                newRate.getRateType()
                        );

                // ==== AVERAGERATE – undvik dubletter exakt efter datum ====
                if (newRate.getRateType() == RateType.AVERAGERATE) {

                    boolean duplicate = previous.stream().anyMatch(p ->
                            p.getEffectiveDate().equals(newRate.getEffectiveDate()) &&
                                    p.getRatePercent().compareTo(newRate.getRatePercent()) == 0
                    );

                    if (duplicate) {
                        continue; // hoppa över denna
                    }
                }

                // ==== LISTRATE – hantera rateChange + lastChanged (ALTERNATIV A) ====
                if (!previous.isEmpty()) {

                    MortgageRate latest = previous.get(0);

                    boolean isNewerDate = newRate.getEffectiveDate().isAfter(latest.getEffectiveDate());
                    boolean differentValue = newRate.getRatePercent().compareTo(latest.getRatePercent()) != 0;

                    if (isNewerDate && differentValue) {
                        // Räntan ändras på riktigt
                        newRate.setRateChange(newRate.getRatePercent().subtract(latest.getRatePercent()));
                        newRate.setLastChangedDate(newRate.getEffectiveDate());
                    } else {
                        // Räntan är oförändrad → behåll senaste lastChangedDate
                        newRate.setRateChange(latest.getRateChange());
                        newRate.setLastChangedDate(latest.getLastChangedDate());
                    }
                }

                finalRates.add(newRate);
            }


            // Save if anything new
            if (!finalRates.isEmpty()) {
                mortgageRateRepository.saveAll(finalRates);
                savedCount = finalRates.size();
            }

        } catch (Exception e) {
            error = e.getMessage();
        }

        long duration = System.currentTimeMillis() - start;

        // 5. Logging
        rateUpdateLogService.logUpdate(
                bank,
                "ScraperService",
                savedCount,
                error == null,
                error,
                duration
        );

        // 6. Result return
        return new ScraperResult(
                bank.getName(),
                savedCount,
                error == null,
                error,
                duration
        );
    }

    // ==========================================================
    // SCRAPER MATCHING
    // ==========================================================

    public BankScraper getScraperForBank(Bank bank) {
        String norm = normalize(bank.getName());

        return scrapers.stream()
                .filter(s -> {
                    String simple = normalize(s.getClass().getSimpleName());
                    String display = normalize(s.toString());
                    return simple.contains(norm)
                            || display.contains(norm)
                            || norm.contains(simple);
                })
                .findFirst()
                .orElse(null);
    }

    private String normalize(String t) {
        if (t == null) return "";
        return t.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}