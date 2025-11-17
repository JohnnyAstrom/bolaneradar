package com.bolaneradar.backend.controller.analytics;

import com.bolaneradar.backend.dto.analytics.RateTrendDto;
import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.dto.mapper.RateTrendMapper;
import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.analytics.RateAnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 *  RATE ANALYTICS CONTROLLER
 * <p>
 *  Detta lager ansvarar enbart för:
 *   - Ta emot HTTP-anrop
 *   - Skicka vidare parametrar till service-lagret
 *   - Konvertera entiteter till DTOs
 *   - Returnera API-svar
 * <p>
 *  All domänlogik ligger i RateAnalyticsService.
 */
@Tag(name = "Rate Analytics", description = "Analys, historik och trender för bolåneräntor")
@RestController
@RequestMapping("/api/rates/analytics")
public class RateAnalyticsController {

    private final RateAnalyticsService analyticsService;

    public RateAnalyticsController(RateAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ======================================================
    // GET /history/bank/{bankId}
    // ======================================================
    @Operation(
            summary = "Historik för en bank",
            description = "Returnerar filtrerad och sorterad historik för en bank."
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

        List<MortgageRate> rates =
                analyticsService.getRateHistoryForBank(bankId, from, to, sort, rateType, term);

        // Controller konverterar entiteter → DTO (standard i ditt projekt)
        return ResponseEntity.ok(
                rates.stream().map(MortgageRateMapper::toDto).toList()
        );
    }

    // ======================================================
    // GET /history/all-banks
    // ======================================================
    @Operation(
            summary = "Historik för alla banker",
            description = "Returnerar historik per bank i ett Map<BankNamn, List<MortgageRateDto>>."
    )
    @GetMapping("/history/all-banks")
    public ResponseEntity<Map<String, List<MortgageRateDto>>> getAllBanksRateHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "asc") String sort
    ) {
        Map<String, List<MortgageRate>> history =
                analyticsService.getAllBanksRateHistory(from, to, sort);

        // Mappar varje lista av entiteter till en lista av DTOs
        Map<String, List<MortgageRateDto>> dtoMap = history.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(MortgageRateMapper::toDto).toList()
                ));

        return ResponseEntity.ok(dtoMap);
    }

    // ======================================================
    // GET /trends
    // ======================================================
    @Operation(
            summary = "Trender mellan två mättillfällen",
            description = "Returnerar automatiskt beräknade ränteförändringar."
    )
    @GetMapping("/trends")
    public List<RateTrendDto> getRateTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) RateType rateType
    ) {
        List<RateTrend> trends = analyticsService.getRateTrends(from, to, rateType);
        return trends.stream().map(RateTrendMapper::toDto).toList();
    }

    // ======================================================
    // GET /trends/range
    // ======================================================
    @Operation(
            summary = "Alla trender inom datumintervall",
            description = "Analyserar alla förändringar mellan alla datapunkter."
    )
    @GetMapping("/trends/range")
    public List<RateTrendDto> getRateTrendsInRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) RateType rateType
    ) {
        List<RateTrend> trends = analyticsService.getRateTrendsInRange(from, to, rateType);
        return trends.stream().map(RateTrendMapper::toDto).toList();
    }
}