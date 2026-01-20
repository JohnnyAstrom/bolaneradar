package com.bolaneradar.backend.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * ================================================================
 * HEALTH CONTROLLER
 * ================================================================
 *
 * Ansvar:
 * - Exponerar ett enkelt health-check endpoint
 * - Används för drift, övervakning och cold-start warmup
 *
 * Användning:
 * - Frontend warmup (Render cold start)
 * - Kontroll av att backend-processen är tillgänglig
 *
 * Notering:
 * - Innehåller ingen affärslogik
 * - Gör inga databas- eller service-anrop
 * ================================================================
 */
@Tag(name = "System / Health")
@RestController
public class HealthController {

    @Operation(summary = "Health check för backend (status & timestamp)")
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}