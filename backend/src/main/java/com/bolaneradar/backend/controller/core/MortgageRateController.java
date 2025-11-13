package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.MortgageRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller för att hantera aktuella bolåneräntor.
 * CRUD / hämtning – ingen analys eller historik.
 */
@Tag(name = "Mortgage Rates", description = "CRUD och hämtning av bolåneräntor")
@RestController
@RequestMapping("/api/rates")
public class MortgageRateController {

    private final MortgageRateService mortgageRateService;
    private final BankService bankService;

    public MortgageRateController(MortgageRateService mortgageRateService,
                                  BankService bankService) {
        this.mortgageRateService = mortgageRateService;
        this.bankService = bankService;
    }

    // ======================================================
    // GET /api/rates – alla räntor
    // ======================================================
    @Operation(summary = "Hämta alla aktuella bolåneräntor")
    @GetMapping
    public List<MortgageRateDto> getAllRates() {
        return mortgageRateService.getAllRates()
                .stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    // ======================================================
    // GET /api/rates/bank/{bankId} – räntor per bank
    // ======================================================
    @Operation(summary = "Hämta alla räntor för en specifik bank")
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRatesByBank(@PathVariable Long bankId) {

        var optionalBank = bankService.getBankById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bank bank = optionalBank.get();

        List<MortgageRateDto> dtos = mortgageRateService.getRatesByBank(bank)
                .stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // ======================================================
    // POST /api/rates – skapa / uppdatera räntor
    // ======================================================
    @Operation(summary = "Skapa eller uppdatera en lista av bolåneräntor")
    @PostMapping
    public ResponseEntity<List<MortgageRateDto>> createRates(@RequestBody List<MortgageRateDto> rateDtos) {

        // createRate hanterar själva logiken (inkl. validering av bank)
        List<MortgageRateDto> created = rateDtos.stream()
                .map(mortgageRateService::createRate)
                .toList();

        return ResponseEntity.status(201).body(created);
    }
}