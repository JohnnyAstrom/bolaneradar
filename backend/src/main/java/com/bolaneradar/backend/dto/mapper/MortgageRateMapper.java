package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.entity.MortgageRate;

/**
 * Mapper som konverterar MortgageRate-entiteter till MortgageRateDto.
 * Används av controller-lagret vid GET-anrop för att returnera
 * läsbara objekt utan att exponera JPA-entiteter direkt.
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