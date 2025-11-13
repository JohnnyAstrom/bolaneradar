package com.bolaneradar.backend.dto.core;

import java.time.LocalDateTime;

/**
 * DTO för att skicka ut loggdata till klienten.
 * Innehåller relevant information utan att exponera hela Bank-objektet.
 */
public record RateUpdateLogDto(
        Long id,
        String bankName,
        String sourceName,
        int importedCount,
        boolean success,
        String errorMessage,
        long durationMs,
        LocalDateTime occurredAt
) {}