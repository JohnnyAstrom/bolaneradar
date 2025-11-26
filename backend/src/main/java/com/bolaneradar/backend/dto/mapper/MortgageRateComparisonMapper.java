package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.core.MortgageRateComparisonDto;
import com.bolaneradar.backend.entity.core.MortgageRate;

import java.time.LocalDate;

/**
 * Mapper som bygger MortgageRateComparisonDto för startsidans jämförelsetabell.
 *
 * Mappern ansvarar endast för:
 *   - Null-hantering
 *   - BigDecimal → Double-konvertering
 *   - LocalDate → String
 *   - Att skapa en ren DTO från rådata som redan bearbetats i service-lagret
 *
 * Ingen affärslogik får placeras här.
 */
public class MortgageRateComparisonMapper {

    /**
     * Skapar DTO baserat på:
     *  - Senaste listränta
     *  - Senaste snittränta
     *  - Beräknad diff (skickas från service)
     *  - lastChanged-datum (skickas från service)
     */
    public static MortgageRateComparisonDto toDto(
            String bankName,
            MortgageRate listRate,
            MortgageRate avgRate,
            Double diff,
            LocalDate lastChanged
    ) {

        // Listränta
        Double listRateValue =
                (listRate != null && listRate.getRatePercent() != null)
                        ? listRate.getRatePercent().doubleValue()
                        : null;

        // Snittränta
        Double avgRateValue =
                (avgRate != null && avgRate.getRatePercent() != null)
                        ? avgRate.getRatePercent().doubleValue()
                        : null;

        // lastChanged som String (yyyy-MM-dd)
        String lastChangedValue =
                (lastChanged != null)
                        ? lastChanged.toString()
                        : null;

        return new MortgageRateComparisonDto(
                bankName,
                listRateValue,
                avgRateValue,
                diff,
                lastChangedValue
        );
    }
}