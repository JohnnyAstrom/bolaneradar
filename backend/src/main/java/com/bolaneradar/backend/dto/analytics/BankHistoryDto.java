package com.bolaneradar.backend.dto.analytics;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import java.util.List;

/**
 * DTO som representerar historisk räntedata för en bank,
 * grupperad per bindningstid (term) och räntetyp.
 */
public record BankHistoryDto(
        String bankName,
        MortgageTerm term,
        RateType rateType,
        List<MortgageRateHistoryPointDto> history
) {}