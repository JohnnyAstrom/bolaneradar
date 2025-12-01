package com.bolaneradar.backend.controller.admin.rates;

import com.bolaneradar.backend.dto.admin.MortgageRateDto;
import com.bolaneradar.backend.service.admin.MortgageRateAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Admin / Rates")
@RequestMapping("/api/admin/rates")
public class AdminMortgageRateController {

    private final MortgageRateAdminService adminService;

    public AdminMortgageRateController(MortgageRateAdminService adminService) {
        this.adminService = adminService;
    }

    // ======================================================
    // POST /api/admin/rates – skapa eller uppdatera räntor
    // ======================================================
    @Operation(summary = "Skapa eller uppdatera bolåneräntor (admin)")
    @PostMapping
    public ResponseEntity<List<MortgageRateDto>> createRates(
            @RequestBody(required = true) List<MortgageRateDto> rateDtos
    ) {
        if (rateDtos == null || rateDtos.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<MortgageRateDto> created = adminService.createRates(rateDtos);
        return ResponseEntity.status(201).body(created);
    }
}