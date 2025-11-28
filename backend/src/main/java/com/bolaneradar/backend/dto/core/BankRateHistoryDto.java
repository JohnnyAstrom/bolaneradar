package com.bolaneradar.backend.dto.core;

public record BankRateHistoryDto(
        String month,
        Double avgRate
) {}