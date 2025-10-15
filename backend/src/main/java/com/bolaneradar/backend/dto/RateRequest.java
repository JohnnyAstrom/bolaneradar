package com.bolaneradar.backend.dto;

import com.bolaneradar.backend.model.MortgageTerm;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) för att skapa en ny bolåneränta.
 * Motsvarar JSON-strukturen klienten skickar in till /api/rates.
 */
public record RateRequest(
        Long bankId,
        MortgageTerm term,
        BigDecimal ratePercent,
        LocalDate effectiveDate
) {}