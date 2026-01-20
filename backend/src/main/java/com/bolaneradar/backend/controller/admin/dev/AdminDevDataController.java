package com.bolaneradar.backend.controller.admin.dev;

import com.bolaneradar.backend.service.admin.AdminDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * ADMIN DEV DATA CONTROLLER
 * ================================================================
 * <p>
 * Utvecklingsverktyg för administrativ datahantering.
 * Endast aktiv i DEV-profil.
 * <p>
 * Används för att:
 * - Importera exempeldata
 * - Rensa databasen
 * - Ta bort räntor för en specifik bank
 * <p>
 * Ska aldrig vara aktiv i produktion.
 * ================================================================
 */
@Profile("dev")  // Endast aktiv i utvecklingsmiljö
@Tag(name = "Admin / Dev Tools")
@RestController
@RequestMapping("/api/admin/dev")
public class AdminDevDataController {

    private final AdminDataService adminDataService;

    public AdminDevDataController(AdminDataService adminDataService) {
        this.adminDataService = adminDataService;
    }

    // =========================================================
    // POST /api/admin/dev/import-example
    // =========================================================
    @Operation(summary = "Importera exempeldata (endast DEV)")
    @PostMapping("/import-example")
    public ResponseEntity<String> importExampleData() {
        adminDataService.importExampleData();
        return ResponseEntity.status(201).body("Exempeldata importerad.");
    }

    // =========================================================
    // DELETE /api/admin/dev/clear
    // =========================================================
    @Operation(summary = "Rensa hela databasen (endast DEV)")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearDatabase() {
        adminDataService.clearDatabase();
        return ResponseEntity.ok("Databasen har tömts.");
    }

    // =========================================================
    // DELETE /api/admin/dev/delete-rates?bankName=...
    // =========================================================
    @Operation(summary = "Ta bort alla räntor för en specifik bank (endast DEV)")
    @DeleteMapping("/delete-rates")
    public ResponseEntity<String> deleteRatesForBank(@RequestParam String bankName) {
        String result = adminDataService.deleteRatesForBank(bankName);
        return ResponseEntity.ok(result);
    }
}