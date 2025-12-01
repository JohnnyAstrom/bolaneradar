package com.bolaneradar.backend.dto.api;

import java.util.List;

public record BankDetailsDto(
        String description,
        String overviewText,
        List<String> bestFor,
        List<String> notFor,
        String primaryCtaLabel,
        String primaryCtaUrl,
        String secondaryCtaLabel,
        String secondaryCtaUrl
) {}