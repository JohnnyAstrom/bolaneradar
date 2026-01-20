package com.bolaneradar.backend.service.client.banks;

import com.bolaneradar.backend.dto.api.BankIntroDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * BANK INTRO SERVICE
 * ================================================================
 * <p>
 * Ansvar:
 * - Tillhandahåller introduktionstext och USP-punkter för banker
 * - Används av banksidor för att visa beskrivande innehåll
 * <p>
 * Användning:
 * - Anropas av publik bank-controller
 * - Levererar språkberoende introduktionsdata per bank
 * <p>
 * Funktionalitet:
 * - Läser statisk bankinformation från JSON-resurs vid uppstart
 * - Stödjer flera språk (sv / en)
 * - Faller tillbaka till svenska om valt språk saknas
 * <p>
 * Designprinciper:
 * - Ingen databasåtkomst (statisk innehållsdata)
 * - All tolkning och språkval sker i service-lagret
 * - DTO används för att isolera frontend från intern struktur
 * <p>
 * Prestanda:
 * - JSON-data laddas en gång vid applikationsstart
 * - Endast uppslag i minnet vid runtime
 * - Mycket låg overhead per anrop
 * ================================================================
 */
@Service
public class BankIntroService {

    private final Map<String, Map<String, Map<String, Object>>> bankData;

    public BankIntroService() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var resource = new ClassPathResource("data/bankIntroData.json");

        bankData = mapper.readValue(resource.getInputStream(), Map.class);
    }

    public BankIntroDto getBankIntro(String bankKey, String language) {
        var bankNode = bankData.get(bankKey.toLowerCase());

        if (bankNode == null) {
            return null;
        }

        String langKey = language.equalsIgnoreCase("EN") ? "en" : "sv";

        var langNode = bankNode.getOrDefault(langKey, bankNode.get("sv"));

        if (langNode == null) {
            return null;
        }

        return new BankIntroDto(
                bankKey,
                (String) langNode.get("description"),
                (List<String>) langNode.get("uspItems")
        );
    }
}