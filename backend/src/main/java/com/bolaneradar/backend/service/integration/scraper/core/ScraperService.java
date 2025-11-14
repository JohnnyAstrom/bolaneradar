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

    public void scrapeAllBanks() {
        System.out.println("Startar skrapning av alla banker");

        List<Bank> banks = bankRepository.findAll();
        List<String> failedBanks = new ArrayList<>();

        for (Bank bank : banks) {
            ScraperResult result = scrapeSingleBankResult(bank.getName());

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

    public String scrapeSingleBank(String bankName) throws Exception {
        ScraperResult result = scrapeSingleBankResult(bankName);
        if (result.success()) {
            return result.importedCount() + " räntor sparade för " + result.bankName();
        }
        throw new Exception(result.error() != null ? result.error() : "Okänt fel vid scraping av " + bankName);
    }

    public ScraperResult scrapeSingleBankResult(String bankName) {
        long startTime = System.currentTimeMillis();

        Optional<Bank> optionalBank = bankRepository.findByNameIgnoreCase(bankName);
        if (optionalBank.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;

            System.err.println("Ingen bank hittades med namn: " + bankName);

            // INGEN logUpdate här (double logging) – FIX
            return new ScraperResult(bankName, 0, false, "Ingen bank hittades med namn: " + bankName, duration);
        }

        Bank bank = optionalBank.get();
        BankScraper scraper = getScraperForBank(bank);
        if (scraper == null) {
            long duration = System.currentTimeMillis() - startTime;

            System.err.println("Ingen scraper hittades för: " + bank.getName());

            // INGEN logUpdate här (double logging) – FIX
            return new ScraperResult(bank.getName(), 0, false,
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

                    if (newRate.getRateType() == RateType.AVERAGERATE) {
                        boolean sameDate = newRate.getEffectiveDate().equals(latest.getEffectiveDate());
                        boolean sameRate = newRate.getRatePercent().compareTo(latest.getRatePercent()) == 0;

                        if (sameDate && sameRate) {
                            System.out.println("Hoppar över oförändrad snittränta för "
                                    + bank.getName() + " (" + newRate.getTerm() + ")");
                            continue;
                        }
                    }

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

            // ENDA logUpdate – FIX
            rateUpdateLogService.logUpdate(
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

        // Returnera bara resultat – ingen ny duration-beräkning (kan skilja sig millisekunder) – FIX
        return new ScraperResult(bank.getName(), importedCount, success, errorMessage,
                System.currentTimeMillis() - startTime);
    }

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

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o");
    }
}