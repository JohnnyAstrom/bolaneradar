package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.service.smartrate.model.SmartRateAnalysisContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SmartRateAnalysisServiceImpl implements SmartRateAnalysisService {

    private final SmartRateMarketDataService marketService;

    public SmartRateAnalysisServiceImpl(SmartRateMarketDataService marketService) {
        this.marketService = marketService;
    }

    // =========================================================================
    //  PUBLIC ANALYSIS ENTRYPOINT — VERSION 1 (Mock)
    // =========================================================================
    @Override
    public SmartRateTestResult analyze(SmartRateTestRequest request) {

        SmartRateAnalysisContext ctx = buildContext(request);

        // ===============================
        // MOCK RESULT (Version 1)
        // ===============================
        return new SmartRateTestResult(
                "MOCK_RESULT",
                ctx.bankName(),
                ctx.analyzedTerm(),
                BigDecimal.valueOf(0.32),
                BigDecimal.valueOf(0.35),
                "Detta är ett mockat resultat. Riktig analys implementeras i Version 2.",
                "Ingen faktisk databeräkning gjordes – detta är en placeholder.",
                "Version 2 kommer innehålla riktiga rekommendationer baserat på marknadsläge och användarens val."
        );
    }

    // =========================================================================
    //  BUILD CONTEXT — Version 1 (Komplett och korrekt)
    // =========================================================================
    private SmartRateAnalysisContext buildContext(SmartRateTestRequest request) {

        Long bankId = request.bankId();
        String bankName = request.bankName();

        // Välj term
        MortgageTerm analyzedTerm = request.hasOffer()
                ? request.offerTerm()
                : request.userCurrentTerm();

        if (analyzedTerm == null) {
            analyzedTerm = MortgageTerm.VARIABLE_3M;
        }

        // Marknadsdata
        BigDecimal bankAvg =
                marketService.getBankAverageRate(bankId, analyzedTerm);

        BigDecimal marketBest =
                marketService.getMarketBestRate(analyzedTerm);

        BigDecimal marketMedian =
                marketService.getMarketMedianRate(analyzedTerm);

        // Historik endast för Flow A + rörlig
        BigDecimal historicVariableRate = null;

        if (!request.hasOffer()
                && analyzedTerm == MortgageTerm.VARIABLE_3M
                && request.rateChangeDate() != null) {

            historicVariableRate =
                    marketService.getHistoricVariableRate(bankId, request.rateChangeDate());
        }

        return new SmartRateAnalysisContext(

                request.hasOffer(),
                bankId,
                bankName,

                // Flow A
                request.userRate(),
                request.userCurrentTerm(),

                // Flow B
                request.offerRate(),
                request.offerTerm(),

                // Preference
                request.userPreference(),

                // Market data
                bankAvg,
                marketBest,
                marketMedian,

                // Historik
                historicVariableRate,

                // Alltid korrekt term
                analyzedTerm
        );
    }
}