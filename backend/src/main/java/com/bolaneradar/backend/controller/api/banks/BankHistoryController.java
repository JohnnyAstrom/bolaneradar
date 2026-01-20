package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankRateHistoryDto;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.service.client.banks.BankHistoryService;
import com.bolaneradar.backend.service.client.banks.resolver.BankKeyResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ================================================================
 * BANK HISTORY CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för historiska snitträntor per bank.
 * Används för grafer och historikvyer i frontend.
 * <p>
 * Stöder både:
 * - Historisk data per bindningstid
 * - Lista över tillgängliga bindningstider
 * ================================================================
 */

@Tag(name = "Public / Bank History")
@RestController
@RequestMapping("/api/banks")
public class BankHistoryController {

    private final BankHistoryService historyService;
    private final BankKeyResolver bankKeyResolver;

    public BankHistoryController(
            BankHistoryService historyService,
            BankKeyResolver bankKeyResolver
    ) {
        this.historyService = historyService;
        this.bankKeyResolver = bankKeyResolver;
    }

    @Operation(summary = "Hämta historiska snitträntor för en bank")
    @GetMapping("/{bankKey}/history/data")
    public List<BankRateHistoryDto> getBankHistory(
            @PathVariable String bankKey,
            @RequestParam MortgageTerm term
    ) {
        String bankName = bankKeyResolver.resolve(bankKey);

        return historyService.getHistoricalAverageRates(bankName, term);
    }

    @Operation(summary = "Hämta bindningstider med tillräcklig historik")
    @GetMapping("/{bankKey}/history/available-terms")
    public List<MortgageTerm> getAvailableTerms(@PathVariable String bankKey) {

        String bankName = bankKeyResolver.resolve(bankKey);

        return historyService.getAvailableTerms(bankName);
    }
}