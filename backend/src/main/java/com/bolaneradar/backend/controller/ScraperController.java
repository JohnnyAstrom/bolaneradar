package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.service.scraper.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Scraper", description = "Manuella endpoints för att trigga webbskrapning av banker")
@RestController
@RequestMapping("/api/scrape")
public class ScraperController {

    private final ScraperService scraperService;

    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Operation(summary = "Kör scraping för alla banker", description = "Startar webbskrapning för samtliga registrerade banker.")
    @GetMapping("/all")
    public ResponseEntity<String> scrapeAll() throws IOException {
        scraperService.scrapeAllBanks();
        return ResponseEntity.ok("Skrapning av alla banker körd (se loggen för resultat).");
    }

    @Operation(summary = "Kör scraping för en specifik bank", description = "Startar webbskrapning för en viss bank via namn.")
    @GetMapping("/{bankName}")
    public ResponseEntity<String> scrapeSingleBank(@PathVariable String bankName) throws IOException {
        String result = scraperService.scrapeSingleBank(bankName);
        return ResponseEntity.ok(result);
    }
}