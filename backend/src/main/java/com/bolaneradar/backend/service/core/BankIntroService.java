package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.BankIntroDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BankIntroService {

    private Map<String, Map<String, Object>> bankData;

    public BankIntroService() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var resource = new ClassPathResource("data/bankIntroData.json");

        bankData =
                mapper.readValue(resource.getInputStream(), Map.class);
    }

    public BankIntroDto getBankIntro(String bankKey) {
        var raw = bankData.get(bankKey.toLowerCase());

        if (raw == null) {
            return null;
        }

        return new BankIntroDto(
                bankKey,
                (String) raw.get("description"),
                (List<String>) raw.get("uspItems")
        );
    }
}