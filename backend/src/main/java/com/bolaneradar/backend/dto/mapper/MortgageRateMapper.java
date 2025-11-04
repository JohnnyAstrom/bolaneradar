package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.model.MortgageRate;

/**
 * Mapper som konverterar MortgageRate-objekt till MortgageRateDto.
 * Används för att exponera bolåneräntor i API-svaren utan att direkt returnera entiteter.
 */
public class MortgageRateMapper {

    public static MortgageRateDto toDto(MortgageRate rate) {
        return new MortgageRateDto(
                rate.getId(),
                rate.getBank().getName(),
                rate.getTerm(),
                rate.getRateType(),
                rate.getRatePercent(),
                rate.getEffectiveDate(),
                rate.getRateChange(),
                rate.getLastChangedDate()
        );
    }
}