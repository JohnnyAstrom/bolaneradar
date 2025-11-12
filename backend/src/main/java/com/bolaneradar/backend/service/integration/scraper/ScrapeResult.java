package com.bolaneradar.backend.service.integration.scraper;

public record ScrapeResult(
        String bankName,
        int importedCount,
        boolean success,
        String error,
        long durationMs
) {}
