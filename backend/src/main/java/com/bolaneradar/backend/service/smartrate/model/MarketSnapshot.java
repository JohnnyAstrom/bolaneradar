package com.bolaneradar.backend.service.smartrate.model;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;
import java.util.Map;

public record MarketSnapshot(
        Map<MortgageTerm, BigDecimal> bestByTerm,
        Map<MortgageTerm, BigDecimal> medianByTerm,
        Map<MortgageTerm, BigDecimal> bankAvgByTerm
) {}