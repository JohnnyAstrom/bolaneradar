package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import java.math.BigDecimal;

/**
 * Kundens ränteerbjudande som skickas in i SmartRate-testet.
 * Varje objekt beskriver en erbjuden bindningstid och tillhörande ränta.
 */
public record SmartRateOfferDto(

        // Bindningstiden som kunden fått ett erbjudande på (t.ex. FIXED_3Y)
        MortgageTerm term,

        // Den ränta som banken erbjudit för denna bindningstid
        BigDecimal rate
) {}