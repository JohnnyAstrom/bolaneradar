package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankDetailsDto;
import com.bolaneradar.backend.service.client.BankDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Public / Bank Details")
@RestController
@RequestMapping("/api/banks")
@CrossOrigin
public class BankDetailsController {

    private final BankDetailsService bankDetailsService;

    public BankDetailsController(BankDetailsService bankDetailsService) {
        this.bankDetailsService = bankDetailsService;
    }

    @Operation(summary = "Hämta detaljerad bankinformation")
    @GetMapping("/{bankKey}/details")
    public ResponseEntity<?> getBankDetails(@PathVariable String bankKey) {

        BankDetailsDto dto = bankDetailsService.getDetailsForBank(bankKey);

        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }
}