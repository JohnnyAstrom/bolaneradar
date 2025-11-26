package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.service.core.MortgageRateComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ================================================================
 *  MORTGAGE RATE COMPARISON CONTROLLER
 * ================================================================
 *
 *  Denna controller exponerar EN enda endpoint som frontend använder:
 *
 *     GET /api/rates/comparison?term=3m
 *
 *  Backend returnerar ett komplett paket:
 *     - averageMonth            (YYYY-MM-01)
 *     - averageMonthFormatted   ("okt 2025")
 *     - rows                    (alla bankrader)
 *
 *  På så sätt är frontend "dum" och gör ingen logik alls,
 *  utan visar bara värden backend har räknat ut.
 */
@Tag(
        name = "Mortgage Rate Comparison",
        description = "Endpoint för jämförelsetabellen på startsidan"
)
@RestController
@RequestMapping("/api/rates")
public class MortgageRateComparisonController {

    private final MortgageRateComparisonService service;

    public MortgageRateComparisonController(MortgageRateComparisonService service) {
        this.service = service;
    }

    // ============================================================
    // ================       GET /comparison      ================
    // ============================================================

    /**
     * Hämtar ett komplett datatpaket för jämförelsetabellen:
     *
     *   GET /api/rates/comparison?term=3m
     *
     * Returnerar JSON:
     * {
     *   "averageMonth": "2025-10-01",
     *   "averageMonthFormatted": "okt 2025",
     *   "rows": [...]
     * }
     *
     */
    @Operation(
            summary = "Hämta jämförelsedata",
            description = "Returnerar listräntor, snitträntor, ändringsdatum och gemensam snitträntemånad."
    )
    @GetMapping("/comparison")
    public Map<String, Object> getComparison(@RequestParam String term) {
        return service.getComparisonDataFull(term);
    }
}