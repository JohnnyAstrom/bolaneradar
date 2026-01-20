package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankInfoDto;
import com.bolaneradar.backend.entity.enums.Language;
import com.bolaneradar.backend.service.client.banks.BankInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * BANK INFO CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för fördjupad bankinformation.
 * Exponerar innehåll för informations- och faktasidor per bank.
 * <p>
 * Datat hämtas från filbaserad JSON och är språkberoende.
 * ================================================================
 */
@Tag(
        name = "Public / Bank Info",
        description = "Fördjupad information om varje bank (informationssidor)."
)
@RestController
@RequestMapping("/api/banks")
@CrossOrigin
public class BankInfoController {

    private final BankInfoService bankInfoService;

    public BankInfoController(BankInfoService bankInfoService) {
        this.bankInfoService = bankInfoService;
    }

    @Operation(
            summary = "Hämta fördjupad bankinformation",
            description = """
                    Returnerar informationssidan för vald bank.
                    Datat kommer från filbaserad JSON (en fil per bank).

                    Innehåller:
                    - Introduktion
                    - Fördjupade sektioner
                    - FAQ
                    - CTA
                    """
    )
    @GetMapping("/{bankKey}/info")
    public ResponseEntity<?> getBankInfo(
            @PathVariable String bankKey,
            @RequestParam(defaultValue = "SV") Language language
    ) {

        BankInfoDto.Content content =
                bankInfoService.getBankInfo(bankKey, language);

        return (content == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(content);
    }
}