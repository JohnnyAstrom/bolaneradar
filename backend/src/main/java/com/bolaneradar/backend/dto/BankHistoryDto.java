package com.bolaneradar.backend.dto;

import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.model.RateType;
import java.util.List;

/**
 * DTO som representerar historisk räntedata för en bank,
 * grupperad per bindningstid (term) och räntetyp.
 */
public record BankHistoryDto(
        String bankName,
        MortgageTerm term,
        RateType rateType,
        List<RatePointDto> history
) {}