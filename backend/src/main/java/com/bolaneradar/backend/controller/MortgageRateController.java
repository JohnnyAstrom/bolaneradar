package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.BankHistoryDto;
import com.bolaneradar.backend.dto.LatestRateDto;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.service.BankService;
import com.bolaneradar.backend.service.MortgageRateService;
import com.bolaneradar.backend.dto.RateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // Konstruktor med dependency injection för tjänsterna
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
     */
    @PostMapping
    public ResponseEntity<MortgageRate> createRate(@RequestBody RateRequest request) {
        return bankService.getBankById(request.bankId())
                .map(bank -> {
                    MortgageRate rate = new MortgageRate(
                            bank,
                            request.term(),
                            request.rateType(),
                            request.ratePercent(),
                            request.effectiveDate()
                    );
                    MortgageRate saved = mortgageRateService.saveRate(rate);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * GET /api/rates/latest
     * Returnerar den senaste räntan (senaste effectiveDate) per bank.
     */
    @GetMapping("/latest")
    public List<LatestRateDto> getLatestRates() {
        return mortgageRateService.getLatestRatesPerBank();
    }

    /**
     * GET /api/rates/history/{bankId}
     * Hämtar alla historiska räntor för en viss bank.
     * Parametrar:
     * - from: startdatum (frivilligt)
     * - to: slutdatum (frivilligt)
     * - sort: "asc" eller "desc" (default = desc)
     */
    @GetMapping("/history/{bankId}")
    public ResponseEntity<List<LatestRateDto>> getRateHistoryForBank(
            @PathVariable Long bankId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(
                        mortgageRateService.getRateHistoryForBank(bank, from, to, sort)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/rates/history
     * Returnerar alla bankers historiska räntor.
     * Parametrar:
     * - from: startdatum (frivilligt)
     * - to: slutdatum (frivilligt)
     * - sort: "asc" eller "desc" (default = desc)
     */
    @GetMapping("/history")
    public ResponseEntity<List<BankHistoryDto>> getAllBanksRateHistory(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {

        List<Bank> banks = bankService.getAllBanks();
        List<BankHistoryDto> history = mortgageRateService.getAllBanksRateHistory(banks, from, to, sort);
        return ResponseEntity.ok(history);
    }
}
