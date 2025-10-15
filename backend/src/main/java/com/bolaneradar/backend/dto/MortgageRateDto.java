package com.bolaneradar.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MortgageRateDto(
        Long id,
        String term,
        BigDecimal ratePercent,
        LocalDate effectiveDate
) {}