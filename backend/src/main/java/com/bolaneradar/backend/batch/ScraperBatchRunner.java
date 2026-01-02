package com.bolaneradar.backend.batch;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperResult;
import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class ScraperBatchRunner implements ApplicationRunner {

    private final ScraperService scraperService;

    public ScraperBatchRunner(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    private static final Logger log =
            LoggerFactory.getLogger(ScraperBatchRunner.class);

    @Override
    public void run(ApplicationArguments args) {

        // Kör endast om --mode är angivet
        if (!args.containsOption("mode")) {
            return;
        }

        String mode = args.getOptionValues("mode").getFirst();

        long startTime = System.currentTimeMillis();

        System.out.println("========================================");
        System.out.println("Batch start: " + mode);
        System.out.println("Starttid: " + LocalDateTime.now());
        System.out.println("========================================");

        int successCount = 0;
        int failureCount = 0;

        try {

            // =====================================================
            // FULL SCRAPE – alla banker (CI / daglig körning)
            // =====================================================
            if ("scrape".equalsIgnoreCase(mode)) {

                var result = scraperService.scrapeAllBanks();
                successCount = result.successfulBanks();
                failureCount = result.failedBanks();

            }

            // =====================================================
            // ICA ONLY – manuell körning lokalt
            // =====================================================
            else if ("scrape-ica".equalsIgnoreCase(mode)) {

                ScraperResult result =
                        scraperService.runScrapeForBank("ICA Banken");

                if (result.success()) {
                    successCount = 1;
                    failureCount = 0;
                } else {
                    successCount = 0;
                    failureCount = 1;
                }

            }

            // =====================================================
            // OKÄNT MODE
            // =====================================================
            else {
                System.err.println("Okänt batch-mode: " + mode);
                System.exit(2);
            }

        } catch (Exception e) {
            log.error("Batch kraschade oväntat", e);
            System.exit(2);
        }

        long durationMs = System.currentTimeMillis() - startTime;

        System.out.println("========================================");
        System.out.println("Batch klar");
        System.out.println("Lyckade banker: " + successCount);
        System.out.println("Misslyckade banker: " + failureCount);
        System.out.println("Total tid: " + durationMs + " ms");
        System.out.println("Sluttid: " + LocalDateTime.now());
        System.out.println("========================================");

        // Exit codes för automation (CI / cron)
        if (successCount > 0) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}