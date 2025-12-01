package com.bolaneradar.backend.dto.api;

import java.util.List;

public record BankIntroDto(
        String bankKey,
        String description,
        List<String> uspItems
) {}