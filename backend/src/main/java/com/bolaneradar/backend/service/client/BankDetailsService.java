package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.BankDetailsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BankDetailsService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BankDetailsDto getDetailsForBank(String bankKey) {
        try {
            var resource = new ClassPathResource("data/bankDetailsData.json");
            JsonNode root = objectMapper.readTree(resource.getInputStream());

            if (!root.has(bankKey)) {
                return null;
            }

            JsonNode bankNode = root.get(bankKey);

            return objectMapper.treeToValue(bankNode, BankDetailsDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Could not read bankDetailsData.json", e);
        }
    }
}