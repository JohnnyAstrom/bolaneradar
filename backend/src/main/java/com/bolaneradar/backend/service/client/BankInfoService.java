package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.BankInfoDto;
import com.bolaneradar.backend.dto.api.BankInfoDto.Content;
import com.bolaneradar.backend.entity.enums.Language;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class BankInfoService {

    private final Map<String, BankInfoDto> bankInfoMap = new HashMap<>();

    public BankInfoService(ObjectMapper objectMapper) throws IOException {

        String[] banks = {
                "swedbank", "seb", "nordea", "handelsbanken",
                "lansforsakringarbank", "alandsbanken", "sbab", "skandiabanken",
                "danskebank", "icabanken", "landshypotekbank", "ikanobank"
        };

        for (String bank : banks) {
            String path = "/data/bankinfo/" + bank + ".json";
            InputStream is = getClass().getResourceAsStream(path);

            if (is == null) {
                System.err.println("Hittar inte bankinfo-fil: " + path);
                continue;
            }

            BankInfoDto dto = objectMapper.readValue(is, new TypeReference<>() {});
            bankInfoMap.put(bank, dto);
        }
    }

    public Content getBankInfo(String bankKey, Language language) {
        BankInfoDto dto = bankInfoMap.get(bankKey.toLowerCase());

        if (dto == null) {
            return null;
        }

        return switch (language) {
            case EN -> dto.en;
            case SV -> dto.sv;
        };
    }
}