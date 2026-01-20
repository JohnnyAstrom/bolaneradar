package com.bolaneradar.backend.service.client.smartrate.model;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateOfferDto;
import com.bolaneradar.backend.entity.enums.Language;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Intern kontextmodell för Smart Räntetestet.
 * <p>
 * Samlar all indata och härledd state som behövs
 * under analysflödet. Används endast internt av
 * SmartRate-analysmotorn – exponeras aldrig externt.
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
        List<SmartRateOfferDto> offers,

        // ========== Kundens val ==========
        RatePreference userPreference,

        // ========== Marknadsdata ==========
        BigDecimal bankLatestAverage,
        BigDecimal marketBestRate,
        BigDecimal marketMedianRate,

        // ========== Historik ==========
        BigDecimal historicVariableRate,

        // ========== Analyserad term ==========
        MortgageTerm analyzedTerm,

        // ========== Lånebelopp ==========
        BigDecimal loanAmount,

        // ========== Bindningstid kvar ==========
        Integer monthsUntilExpiration,

        // ========== Språk ==========
        Language language
) {

    /**
     * Hjälpmetod:
     * Returnerar true om analysen gäller en bunden ränta,
     * dvs allt UTOM rörlig (3 månader).
     */
    public boolean isFixedTermAnalysis() {
        return analyzedTerm != MortgageTerm.VARIABLE_3M;
    }
}
