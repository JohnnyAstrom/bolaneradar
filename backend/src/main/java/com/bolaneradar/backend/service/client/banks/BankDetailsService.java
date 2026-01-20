package com.bolaneradar.backend.service.client.banks;

import com.bolaneradar.backend.dto.api.BankDetailsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * ================================================================
 * BANK DETAILS SERVICE
 * ================================================================
 * <p>
 * Ansvar:
 * - Levererar fördjupad bankinformation till banksidan
 * - Används för sektionen "Om banken / Detaljer"
 * <p>
 * Datakälla:
 * - Läser statisk JSON-data från classpath (bankDetailsData.json)
 * - Innehåller språkspecifikt innehåll (sv / en)
 * <p>
 * Funktionalitet:
 * - Hämtar detaljer baserat på bankKey
 * - Väljer språkversion baserat på angiven language-parameter
 * - Faller tillbaka till svenska om önskat språk saknas
 * <p>
 * Designprinciper:
 * - Ingen affärslogik eller databasanrop
 * - Tjänar som adapter mellan statisk innehållsdata och API
 * - DTO ansvarar för struktur och validering av utdata
 * <p>
 * Prestanda:
 * - Filen läses vid anrop (liten datamängd)
 * - Försumbar påverkan jämfört med DB-baserade tjänster
 * ================================================================
 */
@Service
public class BankDetailsService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BankDetailsDto getDetailsForBank(String bankKey, String language) {
        try {
            var resource = new ClassPathResource("data/bankDetailsData.json");
            JsonNode root = objectMapper.readTree(resource.getInputStream());

            if (!root.has(bankKey)) {
                return null;
            }

            JsonNode bankNode = root.get(bankKey);

            String langKey = language.equalsIgnoreCase("EN") ? "en" : "sv";

            JsonNode langNode = bankNode.has(langKey)
                    ? bankNode.get(langKey)
                    : bankNode.get("sv");

            if (langNode == null) {
                return null;
            }

            return objectMapper.treeToValue(langNode, BankDetailsDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Could not read bankDetailsData.json", e);
        }
    }
}