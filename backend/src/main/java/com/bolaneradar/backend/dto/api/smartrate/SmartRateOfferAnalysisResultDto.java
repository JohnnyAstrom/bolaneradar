package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import java.math.BigDecimal;

/**
 * Resultat för analys av ett enskilt ränteerbjudande.
 * Används i offer-flow när kunden anger flera erbjudanden.
 */
public record SmartRateOfferAnalysisResultDto(

        // Vilken bindningstid erbjudandet gäller
        MortgageTerm term,

        // Den ränta kunden blivit erbjuden
        BigDecimal offeredRate,

        // Skillnad mot marknadens bästa snittränta
        BigDecimal diffFromBestMarket,

        // Skillnad mot marknadens median-snittränta
        BigDecimal diffFromMedianMarket,

        // Skillnad mot bankens egna snittränta
        BigDecimal diffFromBankAverage,

        // Statusklassificering (t.ex. GREEN, YELLOW, RED)
        String status,

        // Huvudtext som beskriver analysen för detta erbjudande
        String analysisText,

        // Rekommendation baserat på erbjudandets nivå
        String recommendation,

        // Skillnad i årligkostnad jämfört med erbjudandet
        BigDecimal yearlyCostDifference
) {}