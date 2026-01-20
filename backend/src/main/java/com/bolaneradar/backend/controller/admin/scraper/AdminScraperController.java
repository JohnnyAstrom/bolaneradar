package com.bolaneradar.backend.controller.admin.scraper;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * ADMIN SCRAPER CONTROLLER
 * ================================================================
 * <p>
 * Administrativt API för att trigga scraping av bolåneräntor.
 * Används manuellt av admin för att:
 * - Köra scraping för alla banker
 * - Köra scraping för en specifik bank
 * <p>
 * Anropar ScraperService som innehåller all scrapinglogik.
 * Ingen affärslogik finns i controllern.
 * ================================================================
 */
@RestController
@Tag(name = "Admin / Scraper")
@RequestMapping("/api/admin/scrape")
public class AdminScraperController {

    private final ScraperService scraperService;

    public AdminScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    // ============================================================
    // POST /api/admin/scrape/all
    // ============================================================
    @Operation(summary = "Kör scraping för alla banker (admin)")
    @PostMapping("/all")
    public ResponseEntity<String> scrapeAllBanks() {
        try {
            scraperService.scrapeAllBanks();
            return ResponseEntity.ok("Scraping för alla banker slutförd.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Fel vid scraping: " + e.getMessage());
        }
    }

    // ============================================================
    // POST /api/admin/scrape/{bankName}
    // ============================================================
    @Operation(summary = "Kör scraping för en specifik bank (admin)")
    @PostMapping("/{bankName}")
    public ResponseEntity<String> scrapeBank(@PathVariable String bankName) {
        try {
            String result = scraperService.scrapeSingleBank(bankName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Fel vid scraping av " + bankName + ": " + e.getMessage());
        }
    }
}