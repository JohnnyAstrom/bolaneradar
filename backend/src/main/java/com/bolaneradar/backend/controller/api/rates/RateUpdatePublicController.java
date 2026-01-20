package com.bolaneradar.backend.controller.api.rates;

import com.bolaneradar.backend.dto.api.RateUpdateDayDto;
import com.bolaneradar.backend.service.admin.RateUpdateLogService;
import com.bolaneradar.backend.service.client.rates.RateUpdatePublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * RATE UPDATE PUBLIC CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för att visa senaste ränteändringar.
 * Används av frontend för:
 * - Lista ränteuppdateringar per datum
 * - Visa senaste globala uppdateringstid
 * <p>
 * Hämtar data från service-lagret utan egen affärslogik.
 * ================================================================
 */
@Tag(name = "Public / Rate Updates")
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdatePublicController {

    private final RateUpdateLogService logService;
    private final RateUpdatePublicService rateUpdatePublicService;

    public RateUpdatePublicController(
            RateUpdateLogService logService,
            RateUpdatePublicService rateUpdatePublicService
    ) {
        this.logService = logService;
        this.rateUpdatePublicService = rateUpdatePublicService;
    }

    @Operation(summary = "Hämta senaste globala uppdateringstiden")
    @GetMapping("/latest/global")
    public Map<String, String> getLatestGlobalUpdate() {

        LocalDateTime latest = logService.getLatestGlobalUpdate();

        return Map.of(
                "latestScrape",
                latest != null ? latest.toString() : null
        );
    }

    @Operation(summary = "Hämta senaste ränteändringar per bank")
    @GetMapping
    public List<RateUpdateDayDto> getRateUpdates() {
        return rateUpdatePublicService.getRateUpdates();
    }
}