package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.dto.mapper.RateUpdateMapper;
import com.bolaneradar.backend.service.BankService;
import com.bolaneradar.backend.service.RateUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Rate Updates", description = "Endpoints för loggar av ränteuppdateringar")
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdateController {

    private final RateUpdateService rateUpdateService;
    private final BankService bankService;

    public RateUpdateController(RateUpdateService rateUpdateService, BankService bankService) {
        this.rateUpdateService = rateUpdateService;
        this.bankService = bankService;
    }

    @Operation(summary = "Hämta alla uppdateringsloggar", description = "Returnerar alla uppdateringsloggar i systemet.")
    @GetMapping
    public List<RateUpdateLogDto> getAllUpdateLogs() {
        return RateUpdateMapper.toDtoList(rateUpdateService.getAllLogs());
    }

    @Operation(summary = "Hämta loggar för en bank", description = "Returnerar uppdateringsloggar för en specifik bank baserat på ID.")
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<RateUpdateLogDto>> getLogsForBank(@PathVariable Long bankId) {
        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(
                        RateUpdateMapper.toDtoList(rateUpdateService.getLogsForBank(bank))
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}