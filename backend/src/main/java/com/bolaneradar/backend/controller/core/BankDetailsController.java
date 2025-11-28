package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.BankDetailsDto;
import com.bolaneradar.backend.service.core.BankDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banks")
@CrossOrigin
public class BankDetailsController {

    private final BankDetailsService bankDetailsService;

    public BankDetailsController(BankDetailsService bankDetailsService) {
        this.bankDetailsService = bankDetailsService;
    }

    @GetMapping("/{bankKey}/details")
    public ResponseEntity<?> getBankDetails(@PathVariable String bankKey) {

        BankDetailsDto dto = bankDetailsService.getDetailsForBank(bankKey);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }
}