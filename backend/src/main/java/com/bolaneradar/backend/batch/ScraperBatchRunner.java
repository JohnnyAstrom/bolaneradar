package com.bolaneradar.backend.batch;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScraperBatchRunner implements ApplicationRunner {

    private final ScraperService scraperService;

    public ScraperBatchRunner(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Kör endast i batch-läge
        if (!args.containsOption("mode")) {
            return;
        }

        String mode = args.getOptionValues("mode").get(0);
        if (!"scrape".equalsIgnoreCase(mode)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        System.out.println("========================================");
        System.out.println("Batch start: scraping");
        System.out.println("Starttid: " + LocalDateTime.now());
        System.out.println("========================================");

        int successCount = 0;
        int failureCount = 0;

        try {
            // 🔹 Kör scraping (denna metod ska ALDRIG kasta vidare exceptions)
            var result = scraperService.scrapeAllBanks();

            successCount = result.successfulBanks();
            failureCount = result.failedBanks();

        } catch (Exception e) {
            System.err.println("Batch kraschade oväntat");
            e.printStackTrace();
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

        // Exit codes för automation (GitHub Actions / cron / Render)
        if (successCount > 0) {
            System.exit(0); // OK – minst en bank lyckades
        } else {
            System.exit(1); // FEL – alla banker misslyckades
        }
    }
}