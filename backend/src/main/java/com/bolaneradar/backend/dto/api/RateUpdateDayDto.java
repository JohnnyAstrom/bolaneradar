package com.bolaneradar.backend.dto.api;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO som grupperar ränteändringar per datum.
 * <p>
 * Varje instans motsvarar en dag då en eller flera
 * banker ändrade sina listräntor.
 */
public class RateUpdateDayDto {

    private LocalDate date;
    private List<RateUpdateDto> updates;

    public RateUpdateDayDto() {
    }

    public RateUpdateDayDto(LocalDate date, List<RateUpdateDto> updates) {
        this.date = date;
        this.updates = updates;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<RateUpdateDto> getUpdates() {
        return updates;
    }

    public void setUpdates(List<RateUpdateDto> updates) {
        this.updates = updates;
    }
}