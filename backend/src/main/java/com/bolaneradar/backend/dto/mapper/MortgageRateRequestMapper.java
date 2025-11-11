package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.MortgageRateRequestDto;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.service.core.BankService;

/**
 * Mapper som konverterar inkommande MortgageRateRequestDto (från klienten)
 * till en MortgageRate-entitet.
 *
 * Används av controller-lagret vid skapande av nya räntor (POST),
 * och använder BankService för att slå upp korrekt bank innan sparning.
 */
public class MortgageRateRequestMapper {

    public static MortgageRate toEntity(MortgageRateRequestDto dto, BankService bankService) {
        var bank = bankService.getBankByName(dto.bankName())
                .orElseThrow(() -> new IllegalArgumentException("Banken finns inte: " + dto.bankName()));

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
