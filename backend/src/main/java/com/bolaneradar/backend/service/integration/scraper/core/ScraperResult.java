package com.bolaneradar.backend.service.integration.scraper.core;

public record ScraperResult(
        String bankName,
        int importedCount,
        boolean success,
        String error,
        long durationMs
) {}
