package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.service.integration.scraper.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller som hanterar manuella scraping-anrop.
 * Används av administratörer för att hämta nya bolåneräntor från bankernas webbsidor.
 * <p>
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
    // GET /api/scrape/all  -> startar scraping för alla banker
    // ============================================================

    @Operation(summary = "Kör scraping för alla banker",
            description = "Startar webbskrapning för samtliga registrerade banker.")
    @GetMapping("/all")
    public ResponseEntity<String> scrapeAllBanks() {

        try {
            // Starta scraping via service-lagret
            scraperService.scrapeAllBanks();

            // Returnera svar till klienten
            return ResponseEntity.ok("Scraping för alla banker slutförd");

        } catch (Exception e) {
            // Hantera eventuella fel
            String errorMessage = "Ett fel uppstod vid scraping: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    // ============================================================
    // GET /api/scrape/{bankName}  -> scraping för en specifik bank
    // ============================================================

    @Operation(summary = "Kör scraping för en specifik bank",
            description = "Startar webbskrapning för en viss bank via bankens namn.")
    @GetMapping("/{bankName}")
    public ResponseEntity<String> scrapeBank(@PathVariable String bankName) {

        try {
            // Starta scraping för angiven bank
            String resultMessage = scraperService.scrapeSingleBank(bankName);

            // Returnera lyckat svar
            return ResponseEntity.ok(resultMessage);

        } catch (Exception e) {
            // Returnera felmeddelande vid misslyckad scraping
            String errorMessage = "Fel vid scraping av " + bankName + ": " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}
