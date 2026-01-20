package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.service.client.banks.resolver.BankKeyResolver;
import com.bolaneradar.backend.service.client.banks.BankRateReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ================================================================
 * BANK RATE CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för att hämta aktuella räntor för en specifik bank.
 * <p>
 * BankKeyResolver används för att mappa URL-vänliga banknycklar
 * till korrekt banknamn i databasen.
 * ================================================================
 */

@Tag(name = "Public / Bank Rates")
@RestController
@RequestMapping("/api/banks")
public class BankRateController {

    private final BankRateReadService rateReadService;
    private final BankKeyResolver bankKeyResolver;

    public BankRateController(
            BankRateReadService rateReadService,
            BankKeyResolver bankKeyResolver
    ) {
        this.rateReadService = rateReadService;
        this.bankKeyResolver = bankKeyResolver;
    }

    @Operation(summary = "Hämta aktuella räntor för en bank")
    @GetMapping("/{bankKey}/rates")
    public Map<String, Object> getBankRates(@PathVariable String bankKey) {

        String bankName = bankKeyResolver.resolve(bankKey);

        return rateReadService.getRatesForBank(bankName);
    }
}