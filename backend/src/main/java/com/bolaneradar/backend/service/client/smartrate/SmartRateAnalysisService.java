package com.bolaneradar.backend.service.client.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;

/**
 * ================================================================
 * SMART RATE ANALYSIS SERVICE
 * ================================================================
 * <p>
 * Publikt kontrakt för Smart Räntetest-analysen.
 * <p>
 * Ansvar:
 * - Tar emot användarens input (SmartRateTestRequest)
 * - Returnerar ett färdigt analysresultat för frontend
 * <p>
 * Notering:
 * - Implementationen innehåller all affärslogik
 * - Detta interface används av controller-lagret
 * ================================================================
 */
public interface SmartRateAnalysisService {

    SmartRateTestResult analyze(SmartRateTestRequest request);
}