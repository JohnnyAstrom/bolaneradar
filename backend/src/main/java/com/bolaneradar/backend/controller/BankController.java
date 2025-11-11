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

@Tag(name = "Banks", description = "Endpoints för att hantera banker i systemet")
@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @Operation(summary = "Hämta alla banker", description = "Returnerar en lista över alla banker i databasen.")
    @GetMapping
    public List<BankDto> getAllBanks() {
        return bankService.getAllBanks()
                .stream()
                .map(BankMapper::toDto)
                .toList();
    }

    @Operation(summary = "Hämta en specifik bank", description = "Returnerar en bank baserat på dess ID.")
    @GetMapping("/{id}")
    public ResponseEntity<BankDto> getBankById(@PathVariable Long id) {
        return bankService.getBankById(id)
                .map(bank -> ResponseEntity.ok(BankMapper.toDto(bank)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Skapa en ny bank", description = "Lägger till en ny bank i systemet.")
    @PostMapping
    public ResponseEntity<BankDto> createBank(@RequestBody BankDto bankDto) {
        Bank bank = BankMapper.toEntity(bankDto);
        Bank savedBank = bankService.saveBank(bank);
        return ResponseEntity.status(201).body(BankMapper.toDto(savedBank)); // 201 Created
    }

    @Operation(summary = "Radera en bank", description = "Tar bort en bank baserat på dess ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
