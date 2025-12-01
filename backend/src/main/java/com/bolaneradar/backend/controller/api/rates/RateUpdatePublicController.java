package com.bolaneradar.backend.controller.api.rates;

import com.bolaneradar.backend.service.admin.RateUpdateLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Public / Rate Updates")
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdatePublicController {

    private final RateUpdateLogService logService;

    public RateUpdatePublicController(RateUpdateLogService logService) {
        this.logService = logService;
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
}