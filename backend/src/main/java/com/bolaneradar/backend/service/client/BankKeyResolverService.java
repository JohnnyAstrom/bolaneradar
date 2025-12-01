package com.bolaneradar.backend.service.client;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BankKeyResolverService {

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
        String normalized = bankKey.toLowerCase().replace(" ", "");
        return BANK_KEY_MAP.getOrDefault(normalized, bankKey);
    }
}