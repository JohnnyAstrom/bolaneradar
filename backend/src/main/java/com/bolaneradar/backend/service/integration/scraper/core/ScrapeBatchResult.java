package com.bolaneradar.backend.service.integration.scraper.core;

/**
 * Sammanfattning av en batch-körning.
 */
public record ScrapeBatchResult(
        int successfulBanks,
        int failedBanks
) {}