package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller som hanterar manuella scraping-anrop.
 * Används av administratörer för att hämta nya bolåneräntor från bankernas webbsidor.
 *
 * Flöde: Controller -> ScraperService -> (web scraping) -> ResponseEntity
 */
@Tag(name = "Scraper", description = "Manuella endpoints för att trigga webbskrapning av banker")
@RestController
@RequestMapping("/api/scrape")
public class ScraperController {

    private final ScraperService scraperService;

    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    // ============================================================
    // POST /api/scrape/all  -> startar scraping för alla banker
    // ============================================================

    @Operation(
            summary = "Kör scraping för alla banker",
            description = "Startar webbskrapning för samtliga registrerade banker."
    )
    @PostMapping("/all")
    public ResponseEntity<String> scrapeAllBanks() {
        try {
            scraperService.scrapeAllBanks();
            return ResponseEntity.ok("Scraping för alla banker slutförd");
        } catch (Exception e) {
            String errorMessage = "Ett fel uppstod vid scraping: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    // ============================================================
    // POST /api/scrape/{bankName} -> scraping för specifik bank
    // ============================================================

    @Operation(
            summary = "Kör scraping för en specifik bank (text)",
            description = "Startar webbskrapning för en viss bank och returnerar ett textmeddelande."
    )
    @PostMapping("/{bankName}")
    public ResponseEntity<String> scrapeBankText(@PathVariable String bankName) {
        try {
            String resultMessage = scraperService.scrapeSingleBank(bankName);
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            String errorMessage = "Fel vid scraping av " + bankName + ": " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}