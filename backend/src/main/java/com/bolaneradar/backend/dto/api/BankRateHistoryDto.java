package com.bolaneradar.backend.dto.api;

/**
 * DTO för historisk snittränta per månad.
 * <p>
 * Används för att visa utvecklingen över tid
 * i grafer och diagram.
 */
public record BankRateHistoryDto(
        String month,
        Double avgRate
) {
}