package com.bolaneradar.backend.dto;

import java.util.List;

/**
 * DTO som representerar en banks historiska räntor.
 */
public record BankHistoryDto (
    String bankName,
    List<LatestRateDto> rates
) {}
