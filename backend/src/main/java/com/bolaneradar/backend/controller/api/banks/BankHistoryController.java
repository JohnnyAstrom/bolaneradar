package com.bolaneradar.backend.controller.api.banks;

import com.bolaneradar.backend.dto.api.BankRateHistoryDto;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.service.client.BankHistoryService;
import com.bolaneradar.backend.service.client.BankKeyResolverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Public / Bank History")
@RestController
@RequestMapping("/api/banks")
public class BankHistoryController {

    private final BankHistoryService historyService;
    private final BankKeyResolverService bankKeyResolverService;

    public BankHistoryController(
            BankHistoryService historyService,
            BankKeyResolverService bankKeyResolverService
    ) {
        this.historyService = historyService;
        this.bankKeyResolverService = bankKeyResolverService;
    }

    @Operation(summary = "Hämta historiska snitträntor för en bank")
    @GetMapping("/{bankKey}/history/data")
    public List<BankRateHistoryDto> getBankHistory(
            @PathVariable String bankKey,
            @RequestParam MortgageTerm term
    ) {
        String bankName = bankKeyResolverService.resolve(bankKey);

        return historyService.getHistoricalAverageRates(bankName, term);
    }

    @Operation(summary = "Hämta bindningstider med tillräcklig historik")
    @GetMapping("/{bankKey}/history/available-terms")
    public List<MortgageTerm> getAvailableTerms(@PathVariable String bankKey) {

        String bankName = bankKeyResolverService.resolve(bankKey);

        return historyService.getAvailableTerms(bankName);
    }
}