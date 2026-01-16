package com.bolaneradar.backend.controller.api.rates;

import com.bolaneradar.backend.dto.api.RateUpdateDayDto;
import com.bolaneradar.backend.service.admin.RateUpdateLogService;
import com.bolaneradar.backend.service.client.RateUpdatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Public / Rate Updates")
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdatePublicController {

    private final RateUpdateLogService logService;
    private final RateUpdatesService rateUpdatesService;

    public RateUpdatePublicController(
            RateUpdateLogService logService,
            RateUpdatesService rateUpdatesService
    ) {
        this.logService = logService;
        this.rateUpdatesService = rateUpdatesService;
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
        return rateUpdatesService.getRateUpdates();
    }
}