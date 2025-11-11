package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.dto.mapper.RateUpdateLogMapper;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller för hantering av uppdateringsloggar för bolåneräntor.
 * Exponerar endpoints för att hämta loggar, per bank eller globalt.
 * Använder rateUpdateLogService för logik och RateUpdateLogMapper för DTO-konvertering.
 */
@Tag(name = "Rate Update Logs", description = "Endpoints för loggar av ränteuppdateringar")
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdateLogController {

    private final RateUpdateLogService rateUpdateLogService;
    private final BankService bankService;

    public RateUpdateLogController(RateUpdateLogService rateUpdateLogService, BankService bankService) {
        this.rateUpdateLogService = rateUpdateLogService;
        this.bankService = bankService;
    }

    // ============================================================
    // ===================     HÄMTA LOGGAR     ===================
    // ============================================================

    /**
     * Hämtar alla uppdateringsloggar i systemet.
     * Returnerar en lista med RateUpdateLogDto, sorterade efter datum (senaste först).
     */
    @Operation(
            summary = "Hämta alla uppdateringsloggar",
            description = "Returnerar alla uppdateringsloggar i systemet, sorterade efter datum (senaste först)."
    )
    @GetMapping
    public List<RateUpdateLogDto> getAllUpdateLogs() {
        return rateUpdateLogService.getAllLogs().stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();
    }

    /**
     * Hämtar uppdateringsloggar för en specifik bank baserat på dess ID.
     * Returnerar 404 om banken inte hittas.
     */
    @Operation(
            summary = "Hämta loggar för en bank",
            description = "Returnerar uppdateringsloggar för en specifik bank baserat på dess ID."
    )
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<RateUpdateLogDto>> getLogsForBank(@PathVariable Long bankId) {
        return bankService.getBankById(bankId)
                .map(bank -> {
                    var logs = rateUpdateLogService.getLogsForBank(bank);
                    var dtos = logs.stream()
                            .map(RateUpdateLogMapper::toDto)
                            .toList();
                    return ResponseEntity.ok(dtos);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Hämtar endast den senaste uppdateringen för varje bank.
     * Används t.ex. på adminpanel eller status-sida för snabb översikt.
     */
    @Operation(
            summary = "Hämta senaste uppdateringen per bank",
            description = "Returnerar endast den senaste uppdateringen för varje bank. "
                    + "Används för att snabbt visa senaste status utan att hämta hela logghistoriken."
    )
    @GetMapping("/latest")
    public List<RateUpdateLogDto> getLatestUpdatesPerBank() {
        return rateUpdateLogService.getLatestLogsPerBank().stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();
    }
}