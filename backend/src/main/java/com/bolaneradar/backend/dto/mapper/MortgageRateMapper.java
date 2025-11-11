package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;

/**
 * Mapper som konverterar MortgageRate-entiteter till och från DTO-objekt.
 * Används av controller- och service-lagret för att separera datalager från API-lager.
 */
public class MortgageRateMapper {

    /**
     * Konverterar en MortgageRate-entitet till en MortgageRateDto.
     */
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

    /**
     * Konverterar en MortgageRateDto till en MortgageRate-entitet.
     * Kräver att korrekt Bank redan har hämtats i service-lagret.
     */
    public static MortgageRate toEntity(MortgageRateDto dto, Bank bank) {
        MortgageRate rate = new MortgageRate();
        rate.setBank(bank);
        rate.setTerm(dto.term());
        rate.setRateType(dto.rateType());
        rate.setRatePercent(dto.ratePercent());
        rate.setEffectiveDate(dto.effectiveDate());
        rate.setRateChange(dto.rateChange());
        rate.setLastChangedDate(dto.lastChangedDate());
        return rate;
    }
}