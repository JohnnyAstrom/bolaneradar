package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;
import java.util.List;

/**
 * Huvudresultat-DTO för Smart Räntetestet.
 * <p>
 * Innehåller analys, rekommendationer,
 * jämförelser mot marknaden samt eventuella
 * alternativa räntescenarier.
 */
public record SmartRateTestResult(

        // Statusklassificering utifrån marknaden
        String status,

        // Banknamn
        String bank,

        // Vilken term analysen gjordes på
        MortgageTerm analyzedTerm,

        // Skillnader mot marknad & bank
        BigDecimal differenceFromBankAverage,
        BigDecimal differenceFromBestMarketAverage,

        // Huvudtext som beskriver resultatet
        String analysisText,

        // Förklarande sammanhang
        String additionalContext,

        // Rekommendation baserat på ränteläget
        String recommendation,

        // VERSION 3: Årsbesparing
        BigDecimal yearlySaving,

        // VERSION 3: Rådgivning baserat på kundens preferens
        String preferenceAdvice,

        // VERSION 4: Alternativa räntor att jämföra mot baserat på preferens
        List<SmartRateAlternative> alternatives,

        // Version 5: Alternativesintro
        String alternativesIntro,

        // version 5: Avgöra om det är offertflow eller inte
        boolean isOfferFlow,

        // Analys av varje erbjuden bindningstid.
        // Används i offer-flow när kunden har flera ränteförslag.
        List<SmartRateOfferAnalysisResultDto> offerAnalyses,

        // version 5: Avgöra om det är flera offers
        boolean multipleOffers
) {
}
