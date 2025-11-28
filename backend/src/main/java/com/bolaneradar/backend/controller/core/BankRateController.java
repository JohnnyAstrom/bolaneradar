package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.BankRateHistoryDto;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.service.core.BankRateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankRateController {

    private final BankRateService bankRateService;

    public BankRateController(BankRateService bankRateService) {
        this.bankRateService = bankRateService;
    }

    // --------------------------------------------------------------------
    // 1) Aktuella räntor för banken (list/avg)
    // --------------------------------------------------------------------
    @GetMapping("/{bankName}/rates")
    public Map<String, Object> getBankRates(@PathVariable String bankName) {
        return bankRateService.getRatesForBank(bankName);
    }

    // --------------------------------------------------------------------
    // 2) Historiska snitträntor (för grafen)
    // --------------------------------------------------------------------
    @GetMapping("/{bankName}/history/data")
    public List<BankRateHistoryDto> getBankHistory(
            @PathVariable String bankName,
            @RequestParam MortgageTerm term
    ){
        return bankRateService.getHistoricalAverageRates(bankName, term);
    }

    // --------------------------------------------------------------------
    // 3) Visa vilka bindningstider som har tillräckligt med historik
    // --------------------------------------------------------------------
    @GetMapping("/{bankName}/history/available-terms")
    public List<MortgageTerm> getAvailableTerms(@PathVariable String bankName) {
        return bankRateService.getAvailableTerms(bankName);
    }

}