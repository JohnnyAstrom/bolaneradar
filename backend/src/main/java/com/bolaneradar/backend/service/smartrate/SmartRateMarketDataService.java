package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SmartRateMarketDataService {

    /**
     * Hämtar bankens senaste snittränta (AVERAGERATE)
     * för en given term (t.ex. VARIABLE_3M, FIXED_3Y).
     */
    BigDecimal getBankAverageRate(Long bankId, MortgageTerm term);

    /**
     * Hämtar bästa snitträntan på marknaden för en term.
     * T.ex. den lägsta AVERAGERATE bland alla banker.
     */
    BigDecimal getMarketBestRate(MortgageTerm term);

    /**
     * Hämtar marknadens median snittränta för en term.
     * Alternativt mean om median blir overkill i början.
     */
    BigDecimal getMarketMedianRate(MortgageTerm term);

    /**
     * Hämtar bankens rörliga snittränta (VARIABLE_3M)
     * för den månad då kundens ränta sattes (Q5A).
     */
    BigDecimal getHistoricVariableRate(Long bankId, LocalDate date);

    /**
     * Returnerar vilka bindningstider (MortgageTerm)
     * som motsvarar användarens framtida räntpreferens (Q6A).
     *
     * RORLIG -> [VARIABLE_3M]
     * KORT   -> [FIXED_1Y, FIXED_2Y, FIXED_3Y]
     * LANG   -> [FIXED_4Y ... FIXED_10Y]
     * VET_EJ -> [VARIABLE_3M, FIXED_1Y, FIXED_2Y]
     */
    List<MortgageTerm> getTermsForPreference(RatePreference pref);
}