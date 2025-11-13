package com.bolaneradar.backend.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO som representerar en enskild datapunkt i en banks historiska ränteserie.
 * Innehåller datumet då räntan gällde och själva räntenivån.
 */
public record MortgageRateHistoryPointDto(
        String date,
        BigDecimal rate
) {}