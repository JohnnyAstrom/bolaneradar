package com.bolaneradar.backend.dto;

import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.model.RateType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) för att skapa en ny bolåneränta.
 * Motsvarar JSON-strukturen klienten skickar in till /api/rates.
 */
public record RateRequestDto(
        String bankName,
        MortgageTerm term,
        RateType rateType,
        BigDecimal ratePercent,
        LocalDate effectiveDate,
        BigDecimal rateChange,
        LocalDate lastChangedDate
) {}