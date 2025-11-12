package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.BankDto;
import com.bolaneradar.backend.dto.mapper.BankMapper;
import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.service.core.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller som hanterar alla HTTP-anrop som rör banker.
 *
 * Följer REST-principer:
 * - GET   → hämta data
 * - POST  → skapa ny resurs
 * - DELETE → ta bort resurs
 */
@Tag(name = "Banks", description = "Endpoints för att hantera banker i systemet")
@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // ===========================================================
    // GET /api/banks
    // ===========================================================

    @Operation(summary = "Hämta alla banker", description = "Returnerar en lista över alla banker i databasen.")
    @GetMapping
    public List<BankDto> getAllBanks() {

        // Hämta alla banker som entity-objekt från databasen
        List<Bank> banks = bankService.getAllBanks();

        // Gör om alla Bank-objekt till BankDto med hjälp av mappen
        List<BankDto> dtos = banks.stream()
                .map(BankMapper::toDto)
                .toList();

        // Returnera listan (Spring gör automatiskt om till JSON)
        return dtos;
    }

    // ===========================================================
    // GET /api/banks/{id}
    // ===========================================================

    @Operation(summary = "Hämta en specifik bank", description = "Returnerar en bank baserat på dess ID.")
    @GetMapping("/{id}")
    public ResponseEntity<BankDto> getBankById(@PathVariable Long id) {

        // Hämta banken via service-lagret (returnerar Optional)
        var optionalBank = bankService.getBankById(id);

        // Om banken finns, konvertera till DTO och returnera 200 OK
        if (optionalBank.isPresent()) {
            Bank foundBank = optionalBank.get();
            BankDto dto = BankMapper.toDto(foundBank);
            return ResponseEntity.ok(dto);
        }

        // Om banken inte finns, returnera 404 Not Found
        return ResponseEntity.notFound().build();
    }

    // ===========================================================
    // POST /api/banks
    // ===========================================================

    @Operation(summary = "Skapa en ny bank", description = "Lägger till en ny bank i systemet.")
    @PostMapping
    public ResponseEntity<BankDto> createBank(@RequestBody BankDto bankDto) {

        // Konvertera inkommande JSON (BankDto) till en Bank-entity
        Bank bankEntity = BankMapper.toEntity(bankDto);

        // Skicka entiteten till service-lagret för att spara i databasen
        Bank savedBank = bankService.saveBank(bankEntity);

        // Konvertera tillbaka den sparade entiteten till DTO
        BankDto responseDto = BankMapper.toDto(savedBank);

        // Returnera svaret med status 201 Created
        ResponseEntity<BankDto> response = ResponseEntity.status(201).body(responseDto);
        return response;
    }

    // ===========================================================
    // DELETE /api/banks/{id}
    // ===========================================================

    @Operation(summary = "Radera en bank", description = "Tar bort en bank baserat på dess ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {

        // Be service-lagret ta bort banken
        bankService.deleteBank(id);

        // Returnera status 204 No Content (ingen kropp behövs)
        ResponseEntity<Void> response = ResponseEntity.noContent().build();
        return response;
    }
}
