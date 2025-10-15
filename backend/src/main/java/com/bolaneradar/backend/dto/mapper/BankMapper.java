package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.BankDto;
import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import java.util.List;

public class BankMapper {

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

    private static MortgageRateDto toRateDto(MortgageRate rate) {
        return new MortgageRateDto(
                rate.getId(),
                rate.getTerm().name(), // om term är enum
                rate.getRatePercent(),
                rate.getEffectiveDate()
        );
    }
}