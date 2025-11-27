package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.core.BankRateRowDto;
import com.bolaneradar.backend.entity.core.MortgageRate;

public class BankRateMapper {

    public static BankRateRowDto toDto(
            String termLabel,
            MortgageRate listRate,
            MortgageRate avgRate
    ) {
        return new BankRateRowDto(
                termLabel,
                listRate != null ? listRate.getRatePercent().doubleValue() : null,
                listRate != null && listRate.getRateChange() != null
                        ? listRate.getRateChange().doubleValue()
                        : null,
                avgRate != null ? avgRate.getRatePercent().doubleValue() : null,
                listRate != null && listRate.getLastChangedDate() != null
                        ? listRate.getLastChangedDate().toString()
                        : null
        );
    }
}