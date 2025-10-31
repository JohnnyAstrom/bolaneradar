package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.service.AdminDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Data", description = "Endpoints för att hantera testdata och utvecklingsdata")
@RestController
@RequestMapping("/api/admin")
public class AdminDataController {

    private final AdminDataService adminDataService;

    public AdminDataController(AdminDataService adminDataService) {
        this.adminDataService = adminDataService;
    }

    @Operation(summary = "Importera exempeldata", description = "Skapar exempeldata i databasen för utvecklingstest.")
    @PostMapping("/import-example")
    public ResponseEntity<String> importExampleData() {
        adminDataService.importExampleData();
        return ResponseEntity.status(201).body("Exempeldata importerad framgångsrikt!");
    }

    @Operation(summary = "Rensa databas", description = "Tar bort alla bolåneräntor och loggar (endast för utvecklingsmiljö).")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearDatabase() {
        adminDataService.clearDatabase();
        return ResponseEntity.ok("Databasen har tömts.");
    }
}