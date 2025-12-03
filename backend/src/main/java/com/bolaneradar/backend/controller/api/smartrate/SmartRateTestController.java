package com.bolaneradar.backend.controller.api.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;
import com.bolaneradar.backend.service.smartrate.SmartRateAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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