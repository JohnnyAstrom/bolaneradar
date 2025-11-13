package com.bolaneradar.backend.dto.core;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) för att skapa en ny bolåneränta.
 * Motsvarar JSON-strukturen klienten skickar in till /api/rates.
 */
public record MortgageRateRequestDto(
        String bankName,
        MortgageTerm term,
        RateType rateType,
        BigDecimal ratePercent,
        LocalDate effectiveDate,
        BigDecimal rateChange,
        LocalDate lastChangedDate
) {}