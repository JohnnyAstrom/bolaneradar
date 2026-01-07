package com.bolaneradar.backend.repository.projection;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;

public interface MarketRateSnapshotRow {
    Long getBankId();
    MortgageTerm getTerm();
    BigDecimal getRatePercent();
}