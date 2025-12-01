package com.bolaneradar.backend.controller.api.rates;

import com.bolaneradar.backend.service.client.MortgageRateComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Public / Rate Comparison")
@RestController
@RequestMapping("/api/rates")
public class MortgageRateComparisonController {

    private final MortgageRateComparisonService service;

    public MortgageRateComparisonController(MortgageRateComparisonService service) {
        this.service = service;
    }

    @Operation(
            summary = "Hämta jämförelsedata",
            description = "Returnerar listräntor, snitträntor, ändringsdatum och gemensam snitträntemånad."
    )
    @GetMapping("/comparison")
    public Map<String, Object> getComparison(@RequestParam String term) {
        return service.getComparisonDataFull(term);
    }
}