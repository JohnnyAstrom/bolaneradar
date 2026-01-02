package com.bolaneradar.backend.batch;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ScraperBatchRunner implements ApplicationRunner {

    private final ScraperService scraperService;

    public ScraperBatchRunner(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Kör endast i batch-läge
        if (!args.containsOption("mode")) {
            return;
        }

        String mode = args.getOptionValues("mode").get(0);
        if (!"scrape".equalsIgnoreCase(mode)) {
            return;
        }

        System.out.println("Batch-läge: startar scraping av alla banker");
        scraperService.scrapeAllBanks();
        System.out.println("Batch-läge: scraping klar");

        // Avsluta applikationen när jobbet är klart
        System.exit(0);
    }
}