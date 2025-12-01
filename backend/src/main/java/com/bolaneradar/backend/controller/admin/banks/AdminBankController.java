package com.bolaneradar.backend.controller.admin.banks;

import com.bolaneradar.backend.dto.admin.BankDto;
import com.bolaneradar.backend.dto.mapper.admin.BankMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.service.core.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Admin / Banks")
@RequestMapping("/api/admin/banks")
public class AdminBankController {

    private final BankService bankService;

    public AdminBankController(BankService bankService) {
        this.bankService = bankService;
    }

    // GET /api/admin/banks
    @Operation(summary = "Hämta alla banker (admin)")
    @GetMapping
    public List<BankDto> getAllBanks() {
        return bankService.getAllBanks()
                .stream()
                .map(BankMapper::toDto)
                .toList();
    }

    // GET /api/admin/banks/{id}
    @Operation(summary = "Hämta specifik bank (admin)")
    @GetMapping("/{id}")
    public ResponseEntity<BankDto> getBankById(@PathVariable Long id) {
        return bankService.getBankById(id)
                .map(BankMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/admin/banks
    @Operation(summary = "Skapa ny bank (admin)")
    @PostMapping
    public ResponseEntity<BankDto> createBank(@RequestBody BankDto bankDto) {
        Bank entity = BankMapper.toEntity(bankDto);
        Bank saved = bankService.saveBank(entity);
        return ResponseEntity.status(201).body(BankMapper.toDto(saved));
    }

    // DELETE /api/admin/banks/{id}
    @Operation(summary = "Radera bank (admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build();
    }
}