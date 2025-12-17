package com.bolaneradar.backend.dto.api.smartrate;

import com.bolaneradar.backend.entity.enums.Language;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Requestobjekt för SmartRate-analysen.
 * Innehåller både kundens nuvarande ränta (Flöde A)
 * och eventuella erbjudanden från banken (Flöde B).
 */
public record SmartRateTestRequest(

        // Bankinformation
        Long bankId,
        String bankName,

        // Om kunden har fått ett eller flera ränteerbjudanden
        boolean hasOffer,

        // Lånebelopp (frivilligt men rekommenderat för besparingsberäkningar)
        BigDecimal loanAmount,

        // Språk
        Language language,

        // --------------------------------------------------------------
        // Flöde A — Kundens nuvarande ränta
        // --------------------------------------------------------------

        // Kundens aktuella ränta
        BigDecimal userRate,

        // Bindningstiden för kundens nuvarande ränta
        MortgageTerm userCurrentTerm,

        // När kundens bindningstid löper ut (om relevant)
        LocalDate bindingEndDate,

        // Kundens preferens för framtida bindningstid (rörlig, kort, lång)
        RatePreference userPreference,

        // --------------------------------------------------------------
        // Flöde B — Kundens ränteerbjudanden
        // --------------------------------------------------------------

        // Lista av erbjudanden som kunden har fått från banken
        List<SmartRateOfferDto> offers
) {}