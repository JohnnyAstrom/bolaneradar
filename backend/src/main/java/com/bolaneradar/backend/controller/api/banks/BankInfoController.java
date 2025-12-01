package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankInfoDto;
import com.bolaneradar.backend.service.client.BankInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Public / Bank Info", description = "Fördjupad information om varje bank (informationssidor).")
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
                    - Titel
                    - Sammanfattning
                    - Viktiga punkter (keyPoints)
                    - FAQ
                    - CTA-länk
                    """
    )
    @GetMapping("/{bankKey}/info")
    public ResponseEntity<?> getBankInfo(@PathVariable String bankKey) {

        BankInfoDto dto = bankInfoService.getBankInfo(bankKey);

        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }
}