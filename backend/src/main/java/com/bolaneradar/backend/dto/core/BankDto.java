package com.bolaneradar.backend.dto.core;

import java.util.List;

public record BankDto(
        Long id,
        String name,
        String website,
        List<MortgageRateDto> mortgageRates
) {}