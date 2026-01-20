package com.bolaneradar.backend.service.integration.scraper.core;

/**
 * Resultatobjekt för en scraping-batch.
 * <p>
 * Innehåller en sammanfattning av hur många banker
 * som uppdaterades korrekt respektive misslyckades.
 * <p>
 * Används som returtyp från batch-körningar i scraper-lagret.
 */
public record ScrapeBatchResult(
        int successfulBanks,
        int failedBanks
) {
}