package com.bolaneradar.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO för att skicka ut loggdata till klienten.
 * Innehåller bara relevant information (inte hela Bank-objektet).
 */
public record RateUpdateLogDto(
        Long id,
        String bankName,
        String sourceName,
        int importedCount,
        LocalDateTime occurredAt
) {}
