package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.BankIntroDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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