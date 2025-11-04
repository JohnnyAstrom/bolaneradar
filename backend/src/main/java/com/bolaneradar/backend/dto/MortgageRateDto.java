package com.bolaneradar.backend.dto;

import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.model.RateType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object för att exponera bolåneräntor till frontend.
 */
public record MortgageRateDto(
        Long id,
        String bankName,
        MortgageTerm term,
        RateType rateType,
        BigDecimal ratePercent,
        LocalDate effectiveDate,
        BigDecimal rateChange,
        LocalDate lastChangedDate
) {}