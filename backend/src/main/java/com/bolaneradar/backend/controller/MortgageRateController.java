package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.*;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.dto.mapper.MortgageRateRequestMapper;
import com.bolaneradar.backend.dto.mapper.RateTrendMapper;
import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.core.analytics.RateAnalyticsService;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.core.MortgageRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller för hantering av bolåneräntor.
 * Exponerar REST-endpoints för frontend och kommunicerar via DTO:er.
 * All logik hanteras i service-lagret, och mapping sker via mapper-klasser.
 */
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
    // ===============     GET ENDPOINTS     ================
    // =====================================================

    /**
     * Hämtar alla bolåneräntor som finns registrerade i databasen.
     * Returnerar en lista av MortgageRateDto för presentation i frontend.
     */
    @Operation(summary = "Hämta alla bolåneräntor", description = "Returnerar alla registrerade bolåneräntor i databasen.")
    @GetMapping
    public List<MortgageRateDto> getAllRates() {
        List<MortgageRate> rates = mortgageRateService.getAllRates();
        return rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    /**
     * Hämtar alla räntor för en specifik bank baserat på dess ID.
     * Returnerar en lista av MortgageRateDto eller 404 om banken inte finns.
     */
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

    /**
     * Hämtar de senaste listräntorna (LISTRATE) för alla banker och bindningstider.
     * Sorteras alfabetiskt efter bank och därefter efter term.
     */
    @Operation(summary = "Hämta senaste listräntor", description = "Returnerar de senaste listräntorna per bank och bindningstid.")
    @GetMapping("/latest/listrates")
    public List<MortgageRateDto> getLatestListRates() {
        List<MortgageRate> rates = mortgageRateService.getLatestRatesByType(RateType.LISTRATE);
        return rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    /**
     * Hämtar de senaste snitträntorna (AVERAGERATE) för alla banker och bindningstider.
     */
    @Operation(summary = "Hämta senaste snitträntor", description = "Returnerar de senaste snitträntorna per bank och bindningstid.")
    @GetMapping("/latest/averagerates")
    public List<MortgageRateDto> getLatestAverageRates() {
        List<MortgageRate> rates = mortgageRateService.getLatestRatesByType(RateType.AVERAGERATE);
        return rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    // =====================================================
    // ===============     POST ENDPOINTS     ===============
    // =====================================================

    /**
     * Skapar nya bolåneräntor baserat på inkommande JSON-data.
     * Mottar en lista av RateRequestDto, konverterar till entiteter och sparar.
     * Returnerar de sparade räntorna som DTO:er tillbaka till frontend.
     */
    @Operation(summary = "Skapa nya räntor", description = "Lägger till en eller flera bolåneräntor kopplade till befintliga banker.")
    @PostMapping
    public ResponseEntity<List<MortgageRateDto>> createRates(@RequestBody List<MortgageRateRequestDto> requests) {
        // DTO → Entity
        List<MortgageRate> entities = requests.stream()
                .map(dto -> MortgageRateRequestMapper.toEntity(dto, bankService))
                .toList();

        // Spara entiteter via service
        List<MortgageRate> saved = mortgageRateService.saveAll(entities);

        // Entity → DTO (tillbaka till klienten)
        List<MortgageRateDto> dtos = saved.stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        return ResponseEntity.status(201).body(dtos);
    }

    // =====================================================
    // ============     HISTORIK & TRENDER     =============
    // =====================================================

    /**
     * Hämtar historiska bolåneräntor för en viss bank.
     * Möjlighet att filtrera efter term, räntetyp och datumintervall.
     */
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
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        return bankService.getBankById(bankId)
                .map(bank -> {
                    List<MortgageRate> history = mortgageRateService.getRateHistoryForBank(
                            bank, from, to, sort, rateType, term);
                    List<MortgageRateDto> dtos = history.stream()
                            .map(MortgageRateMapper::toDto)
                            .toList();
                    return ResponseEntity.ok(dtos);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Hämtar historiska bolåneräntor för alla banker inom ett visst intervall.
     * Returnerar en lista av BankHistoryDto för jämförelse mellan banker.
     */
    @Operation(summary = "Hämta historik för alla banker", description = "Returnerar historiska räntor för alla banker.")
    @GetMapping("/history")
    public ResponseEntity<List<BankHistoryDto>> getAllBanksRateHistory(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        List<Bank> banks = bankService.getAllBanks();
        var allHistory = mortgageRateService.getAllBanksRateHistory(banks, from, to, sort);

        // Mappa till BankHistoryDto
        List<BankHistoryDto> dtos = allHistory.entrySet().stream()
                .map(entry -> new BankHistoryDto(entry.getKey(), null, null, // bankName + tomma fält just nu
                        entry.getValue().stream()
                                .map(rate -> new MortgageRateHistoryPointDto(rate.getEffectiveDate().toString(), rate.getRatePercent()))
                                .toList()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // =====================================================
    // ==============     TREND-ANALYSER     ================
    // =====================================================

    /**
     * Beräknar förändringar i bolåneräntor mellan två mättillfällen.
     * Om from/to saknas beräknas skillnader baserat på de senaste datumen.
     */
    @Operation(summary = "Beräkna ränteförändringar", description = "Returnerar skillnader i bolåneräntor mellan två mättillfällen.")
    @GetMapping("/trends")
    public List<RateTrendDto> getRateTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        return rateAnalyticsService.getRateTrends(from, to, rateType)
                .stream()
                .map(RateTrendMapper::toDto)
                .toList();
    }


    /**
     * Beräknar alla ränteförändringar inom ett valt tidsintervall.
     * Visar stegvisa förändringar mellan varje mättillfälle.
     */
    @Operation(summary = "Beräkna förändringar inom intervall", description = "Returnerar alla ränteförändringar inom ett valt tidsintervall.")
    @GetMapping("/trends/range")
    public List<RateTrendDto> getRateTrendsInRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String rateType
    ) {
        return rateAnalyticsService.getRateTrendsInRange(from, to, rateType)
                .stream()
                .map(RateTrendMapper::toDto)
                .toList();
    }
}