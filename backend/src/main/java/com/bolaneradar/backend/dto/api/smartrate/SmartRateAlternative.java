package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;

/**
 * DTO som representerar ett alternativt
 * räntescenario i Smart Räntetestet.
 * <p>
 * Visar hur en annan bindningstid hade
 * påverkat kundens kostnad.
 */
public record SmartRateAlternative(

        // Bindningstid för alternativet
        MortgageTerm term,

        // Bankens snittränta för denna term
        BigDecimal averageRate,

        // Skillnad mot bästa marknadsräntan
        BigDecimal differenceFromBest,

        // Årlig kostnadsökning/sänkning jämfört med marknaden
        BigDecimal yearlyCostDifference
) {
}