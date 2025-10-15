package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.service.BankService;
import com.bolaneradar.backend.service.MortgageRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller för att hantera bolåneräntor.
 * Tar emot HTTP-anrop och skickar vidare till MortgageRateService.
 */
@RestController
@RequestMapping("/api/rates")
public class MortgageRateController {

    private final MortgageRateService mortgageRateService;
    private final BankService bankService;

    // Vi använder både RateService och BankService
    public MortgageRateController(MortgageRateService mortgageRateService, BankService bankService) {
        this.mortgageRateService = mortgageRateService;
        this.bankService = bankService;
    }

    /**
     * GET /api/rates
     * Hämtar alla bolåneräntor i databasen.
     */
    @GetMapping
    public List<MortgageRate> getAllRates() {
        return mortgageRateService.getAllRates();
    }

    /**
     * GET /api/rates/bank/{bankId}
     * Hämtar alla räntor för en specifik bank (via bankens ID).
     */
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<MortgageRate>> getRatesByBank(@PathVariable Long bankId) {
        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(mortgageRateService.getRatesByBank(bank)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/rates
     * Skapar en ny ränta kopplad till en befintlig bank.
     * Exempel på JSON-data som kan skickas:
     * {
     *   "bankId": 1,
     *   "term": "FIXED_3Y",
     *   "ratePercent": 4.85,
     *   "effectiveDate": "2025-10-15"
     * }
     */
    @PostMapping
    public ResponseEntity<MortgageRate> createRate(@RequestBody RateRequest request) {
        return bankService.getBankById(request.bankId())
                .map(bank -> {
                    MortgageRate rate = new MortgageRate(
                            bank,
                            request.term(),
                            request.ratePercent(),
                            request.effectiveDate()
                    );
                    MortgageRate saved = mortgageRateService.saveRate(rate);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * En intern record-klass som beskriver JSON-formatet vi tar emot i POST-anrop.
     * Gör koden tydligare och enklare att förstå.
     */
    public record RateRequest(Long bankId,
                              MortgageRate.RateTerm term,
                              java.math.BigDecimal ratePercent,
                              java.time.LocalDate effectiveDate) {}
}
