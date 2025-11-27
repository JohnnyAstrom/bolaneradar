package com.bolaneradar.backend.dto.core;

public record BankRateRowDto(
        String term,
        Double currentRate,
        Double change,
        Double avgRate,
        String lastChanged
) {}
