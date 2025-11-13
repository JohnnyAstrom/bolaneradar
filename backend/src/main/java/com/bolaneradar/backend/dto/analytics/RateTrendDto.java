package com.bolaneradar.backend.dto.analytics;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representerar förändringen i ränta mellan två mättillfällen.
 */
public record RateTrendDto(
        String bankName,
        String term,
        String rateType,
        double previousRate,
        double currentRate,
        LocalDate fromDate,
        LocalDate toDate,
        double change
) {
    /**
     * Konstruktor som automatiskt beräknar förändringen (change)
     * baserat på currentRate - previousRate, avrundat till två decimaler.
     */
    public RateTrendDto(
            String bankName,
            String term,
            String rateType,
            double previousRate,
            double currentRate,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        this(
                bankName,
                term,
                rateType,
                previousRate,
                currentRate,
                fromDate,
                toDate,
                round(currentRate - previousRate, 2)
        );
    }

    private static double round(double value, int decimals) {
        return BigDecimal.valueOf(value)
                .setScale(decimals, RoundingMode.HALF_UP)
                .doubleValue();
    }
}