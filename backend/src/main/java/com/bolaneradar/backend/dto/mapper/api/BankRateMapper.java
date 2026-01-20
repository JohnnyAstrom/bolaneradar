package com.bolaneradar.backend.dto.mapper.api;

import com.bolaneradar.backend.dto.api.BankRateHistoryDto;
import com.bolaneradar.backend.dto.api.BankRateRowDto;
import com.bolaneradar.backend.entity.core.MortgageRate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ================================================================
 * BANK RATE MAPPER
 * ================================================================
 * Mapper för bankens räntedata som visas på banksidorna.
 * <p>
 * Ansvar:
 * - Översätter MortgageRate-entiteter till enkla DTO:er
 * - Utför endast formatering och konvertering av värden
 * <p>
 * Innehåller:
 * - Mapping för aktuella räntor per bindningstid
 * - Mapping för historiska snitträntor (aggregerade per månad)
 * <p>
 * Designprinciper:
 * - Ingen affärslogik
 * - Ingen databasåtkomst
 * - All beräkning sker i service-lagret
 * ================================================================
 */

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

    public static List<BankRateHistoryDto> toHistoryDto(List<MortgageRate> rates) {

        return rates.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEffectiveDate().withDayOfMonth(1),
                        Collectors.averagingDouble(m -> m.getRatePercent().doubleValue())
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BankRateHistoryDto(
                        e.getKey().toString(),   // "2024-03-01"
                        e.getValue()
                ))
                .toList();
    }
}