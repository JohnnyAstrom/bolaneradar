package com.bolaneradar.backend.dto.api;

public record BankRateHistoryDto(
        String month,
        Double avgRate
) {}