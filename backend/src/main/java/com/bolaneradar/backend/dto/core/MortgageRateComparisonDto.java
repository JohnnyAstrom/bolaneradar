package com.bolaneradar.backend.dto.core;

/**
 * DTO för jämförelsetabellen på startsidan.
 * Kombinerar bankens listränta, snittränta, diff och senaste ändring.
 */
public record MortgageRateComparisonDto(
        String bankName,
        Double listRate,
        Double avgRate,
        Double diff,
        String lastChanged
) {}