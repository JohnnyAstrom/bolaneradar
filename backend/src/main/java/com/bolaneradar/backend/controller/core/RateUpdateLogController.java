package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.RateUpdateLogDto;
import com.bolaneradar.backend.dto.mapper.RateUpdateLogMapper;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller för att hämta uppdateringsloggar för bolåneräntor.
 * Flöde: Controller -> Service -> Mapper -> DTO -> JSON
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
    // GET /api/rates/updates  -> alla loggar
    // ============================================================

    @Operation(
            summary = "Hämta alla uppdateringsloggar",
            description = "Returnerar alla uppdateringsloggar i systemet, sorterade efter datum (senaste först)."
    )
    @GetMapping
    public List<RateUpdateLogDto> getAllUpdateLogs() {
        // Hämta entiteter via service
        var logs = rateUpdateLogService.getAllLogs();

        // Mappa Entity -> DTO
        var dtos = logs.stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();

        // Returnera (Jackson gör DTO -> JSON)
        return dtos;
    }

    // ============================================================
    // GET /api/rates/updates/bank/{bankId}  -> loggar per bank
    // ============================================================

    @Operation(
            summary = "Hämta loggar för en bank",
            description = "Returnerar uppdateringsloggar för en specifik bank baserat på dess ID."
    )
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<RateUpdateLogDto>> getLogsForBank(@PathVariable Long bankId) {
        // Hämta bank
        var optionalBank = bankService.getBankById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 om banken saknas
        }

        var bank = optionalBank.get();

        // Hämta loggar för banken
        var logs = rateUpdateLogService.getLogsForBank(bank);

        // Mappa till DTO
        var dtos = logs.stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();

        // Returnera 200 OK
        return ResponseEntity.ok(dtos);
    }

    // ============================================================
    // GET /api/rates/updates/latest  -> senaste logg per bank
    // ============================================================

    @Operation(
            summary = "Hämta senaste uppdateringen per bank",
            description = "Returnerar endast den senaste uppdateringen för varje bank."
    )
    @GetMapping("/latest")
    public List<RateUpdateLogDto> getLatestUpdatesPerBank() {
        // Hämta senaste logg per bank
        var latestLogs = rateUpdateLogService.getLatestLogsPerBank();

        // Mappa till DTO
        var dtos = latestLogs.stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();

        // Returnera lista
        return dtos;
    }
}