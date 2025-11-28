package com.bolaneradar.backend.dto.core;

import java.util.List;

public record BankIntroDto(
        String bankKey,
        String description,
        List<String> uspItems,
        String primaryCtaLabel,
        String primaryCtaUrl,
        String secondaryCtaLabel,
        String secondaryCtaUrl
) {}