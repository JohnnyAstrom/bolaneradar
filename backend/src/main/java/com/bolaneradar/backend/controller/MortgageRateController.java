package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.*;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.dto.mapper.RateTrendMapper;
import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.MortgageRateService;
import com.bolaneradar.backend.service.core.analytics.RateAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final RateAnalyticsService rateAnalyticsService;

    public MortgageRateController(MortgageRateService mortgageRateService,
                                  BankService bankService,
                                  RateAnalyticsService rateAnalyticsService) {
        this.mortgageRateService = mortgageRateService;
        this.bankService = bankService;
        this.rateAnalyticsService = rateAnalyticsService;
    }

    // =====================================================
    // ===============          GET           ==============
    // =====================================================

    @Operation(summary = "Hämta alla bolåneräntor", description = "Returnerar alla registrerade bolåneräntor i databasen.")
    @GetMapping
    public List<MortgageRateDto> getAllRates() {
        // Hämta entiteter
        List<MortgageRate> rates = mortgageRateService.getAllRates();

        // Mappa Entity -> DTO
        List<MortgageRateDto> dtos = rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        // Returnera (Jackson gör DTO -> JSON)
        return dtos;
    }

    @Operation(summary = "Hämta alla räntor för en bank", description = "Returnerar alla räntor för en viss bank baserat på bankens ID.")
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRatesByBank(@PathVariable Long bankId) {
        // Hämta bank (Optional)
        var optionalBank = bankService.getBankById(bankId);

        // Finns bank?
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bank bank = optionalBank.get();

        // Hämta räntor för bank
        List<MortgageRate> rates = mortgageRateService.getRatesByBank(bank);

        // Mappa till DTO
        List<MortgageRateDto> dtos = rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Hämta senaste listräntor", description = "Returnerar de senaste listräntorna per bank och bindningstid.")
    @GetMapping("/latest/listrates")
    public List<MortgageRateDto> getLatestListRates() {
        List<MortgageRate> rates = mortgageRateService.getLatestRatesByType(RateType.LISTRATE);
        List<MortgageRateDto> dtos = rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();
        return dtos;
    }

    @Operation(summary = "Hämta senaste snitträntor", description = "Returnerar de senaste snitträntorna per bank och bindningstid.")
    @GetMapping("/latest/averagerates")
    public List<MortgageRateDto> getLatestAverageRates() {
        List<MortgageRate> rates = mortgageRateService.getLatestRatesByType(RateType.AVERAGERATE);
        List<MortgageRateDto> dtos = rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();
        return dtos;
    }

    // =====================================================
    // ===============          POST          ==============
    // =====================================================

    @Operation(summary = "Skapa nya räntor", description = "Lägger till en eller flera bolåneräntor kopplade till befintliga banker.")
    @PostMapping
    public ResponseEntity<List<MortgageRateDto>> createRates(@RequestBody List<MortgageRateDto> requests) {
        // 1) För varje inkommande DTO: låt service validera bank, mappa och spara
        List<MortgageRateDto> savedDtos = requests.stream()
                .map(mortgageRateService::createRate) // service sköter DTO -> Entity, bank-koll, save, Entity -> DTO
                .toList();

        // 2) Returnera 201 Created med sparade DTO:er
        ResponseEntity<List<MortgageRateDto>> response = ResponseEntity
                .status(201)
                .body(savedDtos);

        return response;
    }

    // =====================================================
    // ============      HISTORIK & TRENDER     ============
    // =====================================================

    @Operation(
            summary = "Hämta historiska räntor för en viss bank",
            description = "Returnerar historik per term och räntetyp. Filtrera med rateType (LISTRATE/AVERAGERATE), term (t.ex. FIXED_3Y) samt datumintervall."
    )
    @GetMapping("/history/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRateHistoryForBank(
            @PathVariable Long bankId,
            @RequestParam(required = false) RateType rateType,
            @RequestParam(required = false) MortgageTerm term,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "asc") String sort
    ) {
        // Enkel validering av datumintervall
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        // Hämta bank
        var optionalBank = bankService.getBankById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Bank bank = optionalBank.get();

        // Hämta historik via analytics-service
        List<MortgageRate> history = rateAnalyticsService.getRateHistoryForBank(
                bank, from, to, sort, rateType, term);

        // Mappa historik till DTO
        List<MortgageRateDto> dtos = history.stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Operation(
            summary = "Hämta historik för alla banker",
            description = "Returnerar historiska räntor för alla banker."
    )
    @GetMapping("/history")
    public ResponseEntity<List<BankHistoryDto>> getAllBanksRateHistory(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        // Hämta banker
        List<Bank> banks = bankService.getAllBanks();

        // Hämta historik grupperad per bank
        var allHistory = rateAnalyticsService.getAllBanksRateHistory(banks, from, to, sort);

        // Mappa till BankHistoryDto (namn + datapunkter)
        List<BankHistoryDto> dtos = allHistory.entrySet().stream()
                .map(entry -> new BankHistoryDto(
                        entry.getKey(),           // bankName
                        null,                     // rateType (kan utökas)
                        null,                     // term (kan utökas)
                        entry.getValue().stream()
                                .map(rate -> new MortgageRateHistoryPointDto(
                                        rate.getEffectiveDate().toString(),
                                        rate.getRatePercent()
                                ))
                                .toList()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // =====================================================
    // ==============        TREND-ANALYS       ============
    // =====================================================

    @Operation(summary = "Beräkna ränteförändringar", description = "Returnerar skillnader i bolåneräntor mellan två mättillfällen.")
    @GetMapping("/trends")
    public List<RateTrendDto> getRateTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        // Hämta trender (entity-baserade)
        var trends = rateAnalyticsService.getRateTrends(from, to, rateType);

        // Mappa Entity -> DTO
        var dtos = trends.stream()
                .map(RateTrendMapper::toDto)
                .toList();

        return dtos;
    }

    @Operation(summary = "Beräkna förändringar inom intervall", description = "Returnerar alla ränteförändringar inom ett valt tidsintervall.")
    @GetMapping("/trends/range")
    public List<RateTrendDto> getRateTrendsInRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        var trends = rateAnalyticsService.getRateTrendsInRange(from, to, rateType);
        var dtos = trends.stream()
                .map(RateTrendMapper::toDto)
                .toList();
        return dtos;
    }
}