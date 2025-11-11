package com.bolaneradar.backend.dto.mapper;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.entity.RateUpdateLog;

/**
 * Mapper som konverterar RateUpdateLog-entiteter till RateUpdateLogDto.
 * Används av controller-lagret för att returnera loggdata till frontend
 * utan att exponera JPA-relaterad internstruktur.
 */
public class RateUpdateLogMapper {

    public static RateUpdateLogDto toDto(RateUpdateLog log) {
        return new RateUpdateLogDto(
                log.getId(),
                log.getBank() != null ? log.getBank().getName() : null,
                log.getSourceName(),
                log.getImportedCount(),
                log.isSuccess(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getOccurredAt()
        );
    }
}