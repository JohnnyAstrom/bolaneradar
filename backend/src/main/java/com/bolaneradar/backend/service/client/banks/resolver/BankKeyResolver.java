package com.bolaneradar.backend.service.client.banks.resolver;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ================================================================
 * BANK KEY RESOLVER
 * ================================================================
 * <p>
 * Ansvar:
 * - Översätter interna bank-nycklar (bankKey) till visningsnamn
 * - Används som ett hjälplager av bank-relaterade services
 * <p>
 * Funktionalitet:
 * - Normaliserar inkommande bankKey (lowercase, utan mellanslag)
 * - Returnerar ett konsekvent och användarvänligt banknamn
 * <p>
 * Designprinciper:
 * - Innehåller ingen affärslogik
 * - Har inga beroenden till repository eller externa system
 * - Fungerar som en ren uppslags-/helper-klass
 * <p>
 * Arkitektur:
 * - Placeras i banks.resolver för att tydligt skiljas från use-case services
 * - Är inte kopplad till någon specifik controller eller endpoint
 * ================================================================
 */
@Service
public class BankKeyResolver {

    private static final Map<String, String> BANK_KEY_MAP = Map.ofEntries(
            Map.entry("swedbank", "Swedbank"),
            Map.entry("nordea", "Nordea"),
            Map.entry("handelsbanken", "Handelsbanken"),
            Map.entry("seb", "SEB"),
            Map.entry("sbab", "SBAB"),
            Map.entry("icabanken", "ICA Banken"),
            Map.entry("ikanobank", "Ikano Bank"),
            Map.entry("lansforsakringarbank", "Länsförsäkringar Bank"),
            Map.entry("danskebank", "Danske Bank"),
            Map.entry("skandiabanken", "Skandiabanken"),
            Map.entry("landshypotekbank", "Landshypotek Bank"),
            Map.entry("alandsbanken", "Ålandsbanken")
    );

    public String resolve(String bankKey) {
        if (bankKey == null) {
            return null;
        }

        String normalized = bankKey.toLowerCase().replace(" ", "");
        return BANK_KEY_MAP.getOrDefault(normalized, bankKey);
    }
}