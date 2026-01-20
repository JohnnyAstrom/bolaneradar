package com.bolaneradar.backend.controller.api.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;
import com.bolaneradar.backend.service.client.smartrate.SmartRateAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ================================================================
 * SMART RATE TEST CONTROLLER
 * ================================================================
 * <p>
 * Publikt API för Smart Räntetestet.
 * Tar emot användarens räntedata och returnerar
 * en analyserad rekommendation baserad på marknadsdata.
 * <p>
 * Använder SmartRateAnalysisService för all affärslogik.
 * Controller ansvarar endast för HTTP och API-kontrakt.
 * ================================================================
 */
@Tag(name = "Public / Smart Rate Test")
@RestController
@RequestMapping("/api/smartrate")
public class SmartRateTestController {

    private final SmartRateAnalysisService analysisService;

    public SmartRateTestController(SmartRateAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Operation(summary = "Kör Smart Räntetest baserat på användarens input")
    @PostMapping("/test")
    public ResponseEntity<SmartRateTestResult> runSmartRateTest(
            @RequestBody SmartRateTestRequest request
    ) {
        SmartRateTestResult result = analysisService.analyze(request);
        return ResponseEntity.ok(result);
    }
}