package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.core.BankDto;
import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.entity.core.Bank;
import java.util.List;

/**
 * Mapper som konverterar mellan Bank-entiteter och BankDto-objekt.
 * Används för att separera datalager (entities) från API-lagret (DTOs).
 *
 * BankMapper delegerar mappningen av inbäddade MortgageRate-objekt
 * till MortgageRateMapper för att följa principen "en klass – ett ansvar".
 */
public class BankMapper {

    /**
     * Konverterar en Bank-entitet till en BankDto.
     * Inkluderar inbäddade MortgageRateDto-objekt via MortgageRateMapper.
     */
    public static BankDto toDto(Bank bank) {
        List<MortgageRateDto> rates = bank.getMortgageRates().stream()
                .map(MortgageRateMapper::toDto)
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
}