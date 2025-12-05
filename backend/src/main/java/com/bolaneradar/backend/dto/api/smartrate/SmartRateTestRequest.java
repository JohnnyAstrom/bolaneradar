package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SmartRateTestRequest(

        // Bankinfo
        Long bankId,
        String bankName,

        boolean hasOffer,

        // Lånebelopp (frivilligt, men rekommenderat)
        BigDecimal loanAmount,

        // Flöde A
        BigDecimal userRate,
        MortgageTerm userCurrentTerm,
        LocalDate bindingEndDate,
        RatePreference userPreference,

        // Flöde B
        MortgageTerm offerTerm,
        BigDecimal offerRate
) {}