package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.*;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.service.BankService;
import com.bolaneradar.backend.service.MortgageRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Mortgage Rates", description = "Endpoints för att hantera bolåneräntor och trender")
@RestController
@RequestMapping("/api/rates")
public class MortgageRateController {

    private final MortgageRateService mortgageRateService;
    private final BankService bankService;

    public MortgageRateController(MortgageRateService mortgageRateService, BankService bankService) {
        this.mortgageRateService = mortgageRateService;
        this.bankService = bankService;
    }

    @Operation(summary = "Hämta alla bolåneräntor", description = "Returnerar alla registrerade bolåneräntor i databasen.")
    @GetMapping
    public List<MortgageRateDto> getAllRates() {
        return mortgageRateService.getAllRatesAsDto();
    }

    @Operation(summary = "Hämta alla räntor för en bank", description = "Returnerar alla räntor för en viss bank baserat på bankens ID.")
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRatesByBank(@PathVariable Long bankId) {
        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(
                        mortgageRateService.getRatesByBank(bank).stream()
                                .map(MortgageRateMapper::toDto)
                                .toList()
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Skapa en ny ränta", description = "Lägger till en ny bolåneränta kopplad till en befintlig bank.")
    @PostMapping
    public ResponseEntity<MortgageRateDto> createRate(@RequestBody RateRequest request) {
        return bankService.getBankById(request.bankId())
                .map(bank -> {
                    MortgageRate rate = new MortgageRate(
                            bank,
                            request.term(),
                            request.rateType(),
                            request.ratePercent(),
                            request.effectiveDate()
                    );
                    MortgageRate saved = mortgageRateService.saveRate(rate);
                    return ResponseEntity.status(201).body(MortgageRateMapper.toDto(saved)); // 201 Created
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Hämta senaste räntor", description = "Returnerar den senaste räntan per bank och bindningstid.")
    @GetMapping("/latest")
    public List<MortgageRateDto> getLatestRates() {
        return mortgageRateService.getLatestRatesPerBank();
    }

    @Operation(summary = "Hämta historik för en bank", description = "Returnerar en banks historiska räntor mellan valfria datum.")
    @GetMapping("/history/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRateHistoryForBank(
            @PathVariable Long bankId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(
                        mortgageRateService.getRateHistoryForBank(bank, from, to, sort)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Hämta historik för alla banker", description = "Returnerar historiska räntor för alla banker.")
    @GetMapping("/history")
    public ResponseEntity<List<BankHistoryDto>> getAllBanksRateHistory(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        List<Bank> banks = bankService.getAllBanks();
        List<BankHistoryDto> history = mortgageRateService.getAllBanksRateHistory(banks, from, to, sort);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Beräkna ränteförändringar", description = "Returnerar skillnader i bolåneräntor mellan två mättillfällen.")
    @GetMapping("/trends")
    public List<RateTrendDto> getRateTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        return mortgageRateService.getRateTrends(from, to, rateType);
    }

    @Operation(summary = "Beräkna förändringar inom intervall", description = "Returnerar alla ränteförändringar inom ett valt tidsintervall.")
    @GetMapping("/trends/range")
    public List<RateTrendDto> getRateTrendsInRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        return mortgageRateService.getRateTrendsInRange(from, to, rateType);
    }
}