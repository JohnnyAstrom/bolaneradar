package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.analytics.RateTrendDto;
import com.bolaneradar.backend.entity.analytics.RateTrend;

/**
 * Mapper som konverterar interna RateTrend-objekt till RateTrendDto.
 * Används i controller-lagret för att returnera beräknade ränteförändringar
 * i ett format anpassat för frontend eller API-konsumenter.
 */

public class RateTrendMapper {

    public static RateTrendDto toDto(RateTrend trend) {
        return new RateTrendDto(
                trend.getBankName(),
                trend.getTerm(),
                trend.getRateType(),
                trend.getPreviousRate(),
                trend.getCurrentRate(),
                trend.getFromDate(),
                trend.getToDate(),
                trend.getChange()
        );
    }
}