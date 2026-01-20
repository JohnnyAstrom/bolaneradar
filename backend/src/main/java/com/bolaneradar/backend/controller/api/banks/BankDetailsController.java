package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankDetailsDto;
import com.bolaneradar.backend.service.client.banks.BankDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * BANK DETAILS CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för detaljerad bankpresentation.
 * Innehåller beskrivningar, målgrupp, CTA-länkar m.m.
 * <p>
 * Datat är språkstyrt och filbaserat.
 * ================================================================
 */

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
    public ResponseEntity<?> getBankDetails(
            @PathVariable String bankKey,

            @Parameter(
                    description = "Språk för textinnehåll"
            )
            @Schema(
                    example = "SV",
                    allowableValues = { "SV", "EN" }
            )
            @RequestParam(defaultValue = "SV") String language
    ) {

        BankDetailsDto dto =
                bankDetailsService.getDetailsForBank(bankKey, language);

        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }
}