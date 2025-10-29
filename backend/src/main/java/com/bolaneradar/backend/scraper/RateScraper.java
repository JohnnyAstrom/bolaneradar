package com.bolaneradar.backend.scraper;

import org.springframework.stereotype.Component;

@Component
public class RateScraper {

    public void fetchRatesForAllBanks() {
        // TODO: implementera hämtning (HTML/JSON) per bank
        // returnera struktur som DataImportService/RateUpdateService kan spara
        System.out.println("[RateScraper] Fetching rates...");
    }
}
