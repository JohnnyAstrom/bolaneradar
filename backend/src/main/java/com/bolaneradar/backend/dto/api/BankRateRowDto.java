package com.bolaneradar.backend.dto.api;

public record BankRateRowDto(
        String term,
        Double currentRate,
        Double change,
        Double avgRate,
        String lastChanged
) {}
