package com.bolaneradar.backend.dto.api;

import java.util.List;

/**
 * DTO för kort introduktion av en bank.
 * <p>
 * Används för att visa beskrivande text och
 * unika säljpunkter (USP) på banksidor.
 */
public record BankIntroDto(
        String bankKey,
        String description,
        List<String> uspItems
) {
}