package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.OfferComparisonTarget;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SmartRateTestRequest(
        String bank,
        boolean hasOffer,

        // Flöde A
        BigDecimal currentRate,
        MortgageTerm currentRateTerm,
        LocalDate rateChangeDate,
        LocalDate bindingEndDate,
        RatePreference futureRatePreference,

        // Flöde B
        MortgageTerm offerBindingTerm,
        BigDecimal offerRate,
        LocalDate offerStartDate,
        OfferComparisonTarget offerComparisonTarget
) {}