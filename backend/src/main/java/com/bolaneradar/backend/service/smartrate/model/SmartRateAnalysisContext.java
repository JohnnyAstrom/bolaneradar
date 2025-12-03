package com.bolaneradar.backend.service.smartrate.model;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;

/**
 * Intern analysmodell för Smart Räntetestet.
 * Endast för internt bruk i analysmotorn.
 */
public record SmartRateAnalysisContext(

        // ========== Flöde ==========
        boolean hasOffer,

        // ========== Bank ==========
        Long bankId,
        String bankName,

        // ========== Flöde A ==========
        BigDecimal userRate,
        MortgageTerm userCurrentTerm,

        // ========== Flöde B ==========
        BigDecimal offerRate,
        MortgageTerm offerTerm,

        // ========== Kundens val ==========
        RatePreference userPreference,

        // ========== Marknadsdata ==========
        BigDecimal bankLatestAverage,
        BigDecimal marketBestRate,
        BigDecimal marketMedianRate,

        // ========== Historik ==========
        BigDecimal historicVariableRate,

        // ========== Analyserad term ==========
        MortgageTerm analyzedTerm
) {}