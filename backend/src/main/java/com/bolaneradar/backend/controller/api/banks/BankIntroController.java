package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankIntroDto;
import com.bolaneradar.backend.service.client.banks.BankIntroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * BANK INTRO CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för kort introduktionstext per bank.
 * Används för marknads- och översiktstexter i frontend.
 * <p>
 * Datat är filbaserat (JSON) och språkstyrt.
 * ================================================================
 */

@Tag(name = "Public / Bank Intro")
@RestController
@RequestMapping("/api/banks")
@CrossOrigin
public class BankIntroController {

    private final BankIntroService bankIntroService;

    public BankIntroController(BankIntroService bankIntroService) {
        this.bankIntroService = bankIntroService;
    }

    @Operation(summary = "Hämta introduktionsinformation för en bank")
    @GetMapping("/{bankKey}/intro")
    public BankIntroDto getBankIntro(
            @PathVariable String bankKey,

            @Parameter(description = "Språk för textinnehåll")
            @Schema(
                    example = "SV",
                    allowableValues = { "SV", "EN" }
            )
            @RequestParam(defaultValue = "SV") String language
    ) {
        return bankIntroService.getBankIntro(bankKey, language);
    }
}
