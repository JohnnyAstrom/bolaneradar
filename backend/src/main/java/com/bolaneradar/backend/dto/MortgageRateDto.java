package com.bolaneradar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MortgageRateDto(
        Long id,
        String bankName,
        String term,
        String rateType,
        BigDecimal ratePercent,
        LocalDate effectiveDate
) {}