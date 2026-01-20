package com.bolaneradar.backend.dto.api;

/**
 * DTO för en rad i bankens räntetabell.
 * <p>
 * Innehåller aktuell listränta, eventuell förändring,
 * snittränta samt senaste ändringsdatum.
 */
public record BankRateRowDto(
        String term,
        Double currentRate,
        Double change,
        Double avgRate,
        String lastChanged
) {
}
