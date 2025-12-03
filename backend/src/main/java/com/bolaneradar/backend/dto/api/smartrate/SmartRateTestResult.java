package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;

public record SmartRateTestResult(
        String status,
        String bank,
        MortgageTerm analyzedTerm,
        BigDecimal differenceFromBankAverage,
        BigDecimal differenceFromBestMarketAverage,
        String analysisText,
        String additionalContext,
        String recommendation
) {}