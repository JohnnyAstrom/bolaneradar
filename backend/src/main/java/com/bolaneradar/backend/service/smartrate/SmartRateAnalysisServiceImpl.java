package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestRequest;
import com.bolaneradar.backend.dto.api.smartrate.SmartRateTestResult;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SmartRateAnalysisServiceImpl implements SmartRateAnalysisService {

    @Override
    public SmartRateTestResult analyze(SmartRateTestRequest request) {

        // ===== MOCKVERSION – ANALYSMOTORN KOMMER SENARE =====
        // Detta ger dig fullt fungerande flöde (frontend -> backend -> mockresultat)
        // Riktig analys byggs i Version 2.

        // Bestäm vilken term vi ska analysera mot (default för mock)
        MortgageTerm analyzedTerm = request.hasOffer()
                ? request.offerBindingTerm()
                : request.currentRateTerm();

        if (analyzedTerm == null) {
            analyzedTerm = MortgageTerm.VARIABLE_3M;
        }

        return new SmartRateTestResult(
                "MOCK_RESULT",
                request.bank(),
                analyzedTerm,
                BigDecimal.valueOf(0.32),   // differenceFromBankAverage
                BigDecimal.valueOf(0.35),   // differenceFromBestMarketAverage
                "Detta är ett mockat resultat från analysmotorn. " +
                        "Den riktiga logiken för ränteskillnader implementeras i nästa steg.",
                "Ingen verklig data har beräknats ännu. " +
                        "Denna version testar endast flöde och struktur.",
                "Version 2 kommer innehålla riktiga rekommendationer baserat på " +
                        "marknadsläge, bankens snitt och användarens val."
        );
    }
}