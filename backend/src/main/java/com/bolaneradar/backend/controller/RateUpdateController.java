package com.bolaneradar.backend.controller;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.dto.mapper.RateUpdateMapper;
import com.bolaneradar.backend.model.RateUpdateLog;
import com.bolaneradar.backend.service.BankService;
import com.bolaneradar.backend.service.RateUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controller som hanterar HTTP-anrop för loggar av ränteuppdateringar.
 * Gör det möjligt att hämta alla loggar eller loggar för en viss bank.
 */
@RestController
@RequestMapping("/api/rates/updates")
public class RateUpdateController {

    private final RateUpdateService rateUpdateService;
    private final BankService bankService;

    public RateUpdateController(RateUpdateService rateUpdateService, BankService bankService) {
        this.rateUpdateService = rateUpdateService;
        this.bankService = bankService;
    }

    /**
     * GET /api/rates/updates
     * Returnerar alla uppdateringsloggar (senaste först).
     */
    @GetMapping
    public List<RateUpdateLogDto> getAllUpdateLogs() {
        return RateUpdateMapper.toDtoList(rateUpdateService.getAllLogs());
    }


    /**
     * GET /api/rates/updates/bank/{bankId}
     * Returnerar loggar för en specifik bank (senaste först).
     */
    @GetMapping("/bank/{bankId}")
    public ResponseEntity<List<RateUpdateLogDto>> getLogsForBank(@PathVariable Long bankId) {
        return bankService.getBankById(bankId)
                .map(bank -> ResponseEntity.ok(
                        RateUpdateMapper.toDtoList(rateUpdateService.getLogsForBank(bank))
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
