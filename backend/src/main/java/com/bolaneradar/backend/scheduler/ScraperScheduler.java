//package com.bolaneradar.backend.scheduler;
//
//import com.bolaneradar.backend.service.scraper.ScraperService;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.io.IOException;
//
//@Component
//public class ScraperScheduler {
//
//    private final ScraperService scraperService;
//
//    public ScraperScheduler(ScraperService scraperService) {
//        this.scraperService = scraperService;
//    }
//
//    /**
//     * Kör scraping varje dag kl. 09:00.
//     * Cron-format: sekunder, minuter, timmar, dag, månad, veckodag
//     */
//    @Scheduled(cron = "0 30 14 * * *")
//    public void runDailyScraping() throws IOException {
//        System.out.println("Schemalagd scraping startar kl 09:00...");
//        scraperService.scrapeAllBanks();
//        System.out.println("Daglig scraping klar!");
//    }
//}