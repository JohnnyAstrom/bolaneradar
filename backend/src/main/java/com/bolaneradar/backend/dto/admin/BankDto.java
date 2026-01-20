package com.bolaneradar.backend.dto.admin;

/**
 * DTO för administrativa bankoperationer.
 * <p>
 * Används i admin-API:t för att skapa, visa
 * eller uppdatera banker.
 */
public record BankDto(
        Long id,
        String name,
        String website
) {
}
