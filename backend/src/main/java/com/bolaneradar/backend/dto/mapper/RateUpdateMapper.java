package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.model.RateUpdateLog;

import java.util.List;

/**
 * Mapper som konverterar RateUpdateLog-objekt till RateUpdateLogDto.
 */
public class RateUpdateMapper {

    public static RateUpdateLogDto toDto(RateUpdateLog log) {
        String bankName = log.getBank() != null ? log.getBank().getName() : "N/A";

        return new RateUpdateLogDto(
                log.getId(),
                bankName,
                log.getSourceName(),
                log.getImportedCount(),
                log.getOccurredAt()
        );
    }

    public static List<RateUpdateLogDto> toDtoList(List<RateUpdateLog> logs) {
        return logs.stream()
                .map(RateUpdateMapper::toDto)
                .toList();
    }
}