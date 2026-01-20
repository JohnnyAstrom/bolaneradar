package com.bolaneradar.backend.dto.api;

import java.util.List;

/**
 * DTO för detaljerad bankpresentation.
 * <p>
 * Innehåller sammanfattande texter, målgrupp,
 * samt primära och sekundära call-to-action-länkar.
 */
public record BankDetailsDto(
        String description,
        String overviewText,
        List<String> bestFor,
        List<String> notFor,
        String primaryCtaLabel,
        String primaryCtaUrl,
        String secondaryCtaLabel,
        String secondaryCtaUrl
) {
}