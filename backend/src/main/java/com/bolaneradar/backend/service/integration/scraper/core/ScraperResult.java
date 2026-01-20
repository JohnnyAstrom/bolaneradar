package com.bolaneradar.backend.service.integration.scraper.core;

/**
 * Resultat från en enskild bank-scraper.
 * <p>
 * Innehåller utfall, antal importerade räntor
 * samt exekveringstid för banken.
 */
public record ScraperResult(
        String bankName,
        int importedCount,
        boolean success,
        String error,
        long durationMs
) {
}
