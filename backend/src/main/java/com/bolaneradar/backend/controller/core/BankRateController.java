package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.service.core.BankRateService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankRateController {

    private final BankRateService bankRateService;

    public BankRateController(BankRateService bankRateService) {
        this.bankRateService = bankRateService;
    }

    @GetMapping("/{bankName}/rates")
    public Map<String, Object> getBankRates(@PathVariable String bankName) {
        return bankRateService.getRatesForBank(bankName);
    }
}
