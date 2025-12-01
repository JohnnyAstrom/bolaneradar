package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.service.client.BankKeyResolverService;
import com.bolaneradar.backend.service.client.BankRateReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Public / Bank Rates")
@RestController
@RequestMapping("/api/banks")
public class BankRateController {

    private final BankRateReadService rateReadService;
    private final BankKeyResolverService bankKeyResolverService;

    public BankRateController(
            BankRateReadService rateReadService,
            BankKeyResolverService bankKeyResolverService
    ) {
        this.rateReadService = rateReadService;
        this.bankKeyResolverService = bankKeyResolverService;
    }

    @Operation(summary = "Hämta aktuella räntor för en bank")
    @GetMapping("/{bankKey}/rates")
    public Map<String, Object> getBankRates(@PathVariable String bankKey) {

        String bankName = bankKeyResolverService.resolve(bankKey);

        return rateReadService.getRatesForBank(bankName);
    }
}