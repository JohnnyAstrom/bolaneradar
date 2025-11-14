package com.bolaneradar.backend.controller.analytics;

import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.dto.analytics.RateTrendDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.dto.mapper.RateTrendMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.core.BankService;
import com.bolaneradar.backend.service.analytics.RateAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Rate Analytics", description = "Analys, historik och trender för bolåneräntor")
@RestController
@RequestMapping("/api/rates/analytics")
public class RateAnalyticsController {

    private final RateAnalyticsService rateAnalyticsService;
    private final BankService bankService;

    public RateAnalyticsController(RateAnalyticsService rateAnalyticsService,
                                   BankService bankService) {
        this.rateAnalyticsService = rateAnalyticsService;
        this.bankService = bankService;
    }

    // ======================================================
    // GET /api/rates/analytics/history/bank/{bankId}
    // ======================================================
    @Operation(
            summary = "Historik för en bank",
            description = "Returnerar historiska bolåneräntor för en specifik bank med valfria filter."
    )
    @GetMapping("/history/bank/{bankId}")
    public ResponseEntity<List<MortgageRateDto>> getRateHistoryForBank(
            @PathVariable Long bankId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "asc") String sort,
            @RequestParam(required = false) RateType rateType,
            @RequestParam(required = false) MortgageTerm term
    ) {
        var optionalBank = bankService.getBankById(bankId);
        if (optionalBank.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bank bank = optionalBank.get();

        var rates = rateAnalyticsService.getRateHistoryForBank(
                bank, from, to, sort, rateType, term
        );

        var dtos = rates.stream()
                .map(MortgageRateMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // ======================================================
    // GET /api/rates/analytics/history/all-banks
    // ======================================================
    @Operation(
            summary = "Historik för alla banker",
            description = "Returnerar historiska räntor för samtliga banker, grupperat per bank."
    )
    @GetMapping("/history/all-banks")
    public ResponseEntity<Map<String, List<MortgageRateDto>>> getAllBanksRateHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "asc") String sort
    ) {
        List<Bank> banks = bankService.getAllBanks();

        var history = rateAnalyticsService.getAllBanksRateHistory(banks, from, to, sort);

        Map<String, List<MortgageRateDto>> dtoMap = history.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(MortgageRateMapper::toDto)
                                .toList()
                ));

        return ResponseEntity.ok(dtoMap);
    }

    // ======================================================
    // GET /api/rates/analytics/trends
    // ======================================================
    @Operation(
            summary = "Trender mellan två mättillfällen eller senaste automatiskt",
            description = """
                    Beräknar förändringar i bolåneräntor.
                    - Om from/to anges: jämför exakt dessa datum.
                    - Om from/to saknas och rateType=AVERAGERATE: per bank (två senaste datum).
                    - Annars: globala två senaste datum.
                    """
    )
    @GetMapping("/trends")
    public List<RateTrendDto> getRateTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) RateType rateType
    ) {
        List<RateTrend> trends = rateAnalyticsService.getRateTrends(from, to, rateType);
        return trends.stream().map(RateTrendMapper::toDto).toList();
    }

    // ======================================================
    // GET /api/rates/analytics/trends/range
    // ======================================================
    @Operation(
            summary = "Alla trender inom intervall",
            description = "Beräknar alla förändringar i bolåneräntor inom ett valt datumintervall."
    )
    @GetMapping("/trends/range")
    public List<RateTrendDto> getRateTrendsInRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) RateType rateType
    ) {
        List<RateTrend> trends = rateAnalyticsService.getRateTrendsInRange(from, to, rateType);
        return trends.stream().map(RateTrendMapper::toDto).toList();
    }

}