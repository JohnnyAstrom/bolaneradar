package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;

public interface SmartRateAnalysisService {

    SmartRateTestResult analyze(SmartRateTestRequest request);
}