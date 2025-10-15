package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller som hanterar HTTP-anrop relaterade till banker.
 * Tar emot requests och skickar dem vidare till BankService.
 */
@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService bankService;

    // Konstruktor-injektion: Spring skapar automatiskt BankService-objektet åt oss
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    /**
     * GET /api/banks
     * Returnerar en lista över alla banker i databasen.
     */
    @GetMapping
    public List<Bank> getAllBanks() {
        return bankService.getAllBanks();
    }

    /**
     * GET /api/banks/{id}
     * Returnerar en specifik bank om den finns, annars 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bank> getBankById(@PathVariable Long id) {
        return bankService.getBankById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/banks
     * Skapar en ny bank. Datan skickas som JSON i request body.
     * Exempel:
     * {
     *   "name": "Swedbank",
     *   "website": "https://swedbank.se"
     * }
     */
    @PostMapping
    public ResponseEntity<Bank> createBank(@RequestBody Bank bank) {
        Bank savedBank = bankService.saveBank(bank);
        return ResponseEntity.ok(savedBank);  // Returnerar den sparade banken som JSON
    }

    /**
     * DELETE /api/banks/{id}
     * Tar bort en bank via dess ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
