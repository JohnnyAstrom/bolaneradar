package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.BankDto;
import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import java.util.List;

/**
 * Mapper som konverterar mellan Bank-entiteter och BankDto.
 * Används för att separera datalager (entity) från API-lagret.
 *
 * Inkluderar även inbäddad konvertering av MortgageRate till MortgageRateDto
 * för att kunna returnera banker med tillhörande räntor i samma respons.
 */
public class BankMapper {

    /**
     * Konverterar en Bank-entitet till en BankDto.
     */
    public static BankDto toDto(Bank bank) {
        List<MortgageRateDto> rates = bank.getMortgageRates().stream()
                .map(BankMapper::toRateDto)
                .toList();

        return new BankDto(
                bank.getId(),
                bank.getName(),
                bank.getWebsite(),
                rates
        );
    }

    /**
     * Konverterar en BankDto till en Bank-entitet.
     * Används t.ex. vid skapande av ny bank (POST).
     */
    public static Bank toEntity(BankDto dto) {
        Bank bank = new Bank();
        bank.setId(dto.id());
        bank.setName(dto.name());
        bank.setWebsite(dto.website());
        return bank;
    }

    /**
     * Konverterar en MortgageRate till MortgageRateDto (för inbäddad data i BankDto).
     */
    private static MortgageRateDto toRateDto(MortgageRate rate) {
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