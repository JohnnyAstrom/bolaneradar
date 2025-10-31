package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.model.MortgageRate;

public class MortgageRateMapper {

    public static MortgageRateDto toDto(MortgageRate rate) {
        return new MortgageRateDto(
                rate.getId(),
                rate.getBank().getName(),
                rate.getTerm().name(),
                rate.getRateType().name(),
                rate.getRatePercent(),
                rate.getEffectiveDate()
        );
    }
}