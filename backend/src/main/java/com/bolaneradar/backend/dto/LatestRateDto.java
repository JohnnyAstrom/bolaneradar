package com.bolaneradar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO som representerar den senaste räntan per bank.
 */
public record LatestRateDto(
        String bankName,
        String term,
        String rateType,
        BigDecimal ratePercent,
        LocalDate effectiveDate
) {}