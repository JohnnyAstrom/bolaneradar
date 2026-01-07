package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;
import com.bolaneradar.backend.service.smartrate.model.MarketSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface SmartRateMarketDataService {

    /**
     * Hämtar bankens senaste snittränta (AVERAGERATE)
     * för en given term (t.ex. VARIABLE_3M, FIXED_3Y).
     */
    BigDecimal getBankAverageRate(Long bankId, MortgageTerm term);

    /**
     * Hämtar bästa snitträntan på marknaden för en term.
     */
    BigDecimal getMarketBestRate(MortgageTerm term);

    /**
     * Hämtar marknadens median snittränta för en term.
     */
    BigDecimal getMarketMedianRate(MortgageTerm term);

    /**
     * Hämtar bankens rörliga snittränta (VARIABLE_3M)
     * för den månad då kundens ränta sattes.
     */
    BigDecimal getHistoricVariableRate(Long bankId, LocalDate date);

    /**
     * Returnerar vilka bindningstider (MortgageTerm)
     * som motsvarar användarens framtida räntepreferens.
     */
    List<MortgageTerm> getTermsForPreference(RatePreference pref);

    /**
     * Hämtar all marknadsdata som behövs för SmartRate-analys
     * i ett enda anrop, för givna bindningstider.
     *
     * Används för att undvika upprepade DB-anrop per offer.
     */
    MarketSnapshot getMarketSnapshot(
            Long bankId,
            Set<MortgageTerm> terms
    );
}
