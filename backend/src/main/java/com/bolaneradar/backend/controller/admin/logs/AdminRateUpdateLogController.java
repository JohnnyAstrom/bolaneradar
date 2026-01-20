package com.bolaneradar.backend.controller.admin.logs;

import com.bolaneradar.backend.dto.admin.RateUpdateLogDto;
import com.bolaneradar.backend.dto.mapper.admin.RateUpdateLogMapper;
import com.bolaneradar.backend.service.admin.RateUpdateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ================================================================
 * ADMIN RATE UPDATE LOG CONTROLLER
 * ================================================================
 * <p>
 * Administrativt API för att läsa uppdateringsloggar
 * kopplade till scraping och import av bolåneräntor.
 * <p>
 * Stödjer:
 * - Hämtning av alla loggar
 * - Hämtning av senaste logg per bank
 * ================================================================
 */
@RestController
@Tag(name = "Admin / Rate Update Logs")
@RequestMapping("/api/admin/rates/updates")
public class AdminRateUpdateLogController {

    private final RateUpdateLogService rateUpdateLogService;

    public AdminRateUpdateLogController(RateUpdateLogService rateUpdateLogService) {
        this.rateUpdateLogService = rateUpdateLogService;
    }

    // ============================================================
    // GET /api/admin/rates/updates  -> alla loggar
    // ============================================================
    @Operation(summary = "Hämta alla uppdateringsloggar (admin)")
    @GetMapping
    public List<RateUpdateLogDto> getAllUpdateLogs() {
        return rateUpdateLogService.getAllLogs()
                .stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();
    }

    // ============================================================
    // GET /api/admin/rates/updates/latest  -> senaste logg per bank
    // ============================================================
    @Operation(summary = "Hämta senaste uppdateringen per bank (admin)")
    @GetMapping("/latest")
    public List<RateUpdateLogDto> getLatestUpdatesPerBank() {
        return rateUpdateLogService.getLatestLogsPerBank()
                .stream()
                .map(RateUpdateLogMapper::toDto)
                .toList();
    }
}