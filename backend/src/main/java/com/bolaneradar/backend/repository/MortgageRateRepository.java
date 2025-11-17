package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ================================================================
 *  MORTGAGERATEREPOSITORY
 * ================================================================
 *  Detta lager hanterar:
 *    - Alla direkta databasoperationer för MortgageRate
 *    - Både automatiska Spring Data-frågor
 *    - samt anpassade @Query-frågor för mer komplexa behov
 * <p></p>
 *  Repository-lagret ska:
 *    - Aldrig innehålla affärslogik
 *    - Endast tillhandahålla rådata till service-lagret
 * ================================================================
 */
@Repository
public interface MortgageRateRepository extends JpaRepository<MortgageRate, Long> {

    // ========================================================================
    // =============       BANK-SPECIFIKA OPERATIONER       ===================
    // ========================================================================

    /**
     * Hämta alla räntor för en specifik bank.
     */
    List<MortgageRate> findByBank(Bank bank);

    /**
     * Ta bort alla räntor för en bank.
     * Används vid fullständig omimport av historik.
     */
    void deleteByBank(Bank bank);


    // ========================================================================
    // =============       DATUM-BASERADE OPERATIONER       ===================
    // ========================================================================

    /**
     * Hämta alla räntor som gäller för ett specifikt datum.
     * Används bl.a. för att beräkna trender mellan två snapshots.
     */
    List<MortgageRate> findByEffectiveDate(LocalDate effectiveDate);

    /**
     * Hämta räntor mellan två datum (inklusive båda gränser).
     * Används av trendberäkning inom intervall.
     */
    List<MortgageRate> findByEffectiveDateBetween(LocalDate from, LocalDate to);

    /**
     * Hämta alla unika datum där det finns räntedata i databasen.
     * Sorteras senaste först.
     */
    @Query("""
        SELECT DISTINCT m.effectiveDate
        FROM MortgageRate m
        ORDER BY m.effectiveDate DESC
        """)
    List<LocalDate> findDistinctEffectiveDatesDesc();


    // ========================================================================
    // ==========   RATE TYPE + DATUM (GLOBALT ELLER PER BANK)   ==============
    // ========================================================================

    /**
     * Hämta alla räntor av en viss typ (LISTRATE / AVERAGERATE)
     * för ett specifikt datum.
     */
    List<MortgageRate> findByRateTypeAndEffectiveDate(
            RateType rateType,
            LocalDate effectiveDate
    );

    /**
     * Hämta alla räntor för en bank + rateType + specifikt datum.
     * Viktigt för AVERAGERATE-analys per bank.
     */
    List<MortgageRate> findByBankAndRateTypeAndEffectiveDate(
            Bank bank,
            RateType rateType,
            LocalDate effectiveDate
    );

    /**
     * Alla historiska datum för en banks snitträntor/listräntor.
     * Sorteras senaste först.
     * Används av trendflödet (AVERAGERATE per bank).
     */
    @Query("""
        SELECT DISTINCT m.effectiveDate
        FROM MortgageRate m
        WHERE m.bank = :bank
          AND m.rateType = :rateType
        ORDER BY m.effectiveDate DESC
        """)
    List<LocalDate> findDistinctEffectiveDatesByBankAndRateTypeDesc(
            @Param("bank") Bank bank,
            @Param("rateType") RateType rateType
    );


    // ========================================================================
    // =============      BANK + TERM + TYPE (HISTORIK)      ==================
    // ========================================================================

    /**
     * Hämtar historik för en specifik bank + term + rateType.
     * Sorterad med senaste datum först.
     */
    List<MortgageRate> findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
            Bank bank,
            MortgageTerm term,
            RateType rateType
    );

    /**
     * Kontroll om identisk snittränta redan finns.
     * Används för att undvika dubletter vid import.
     */
    boolean existsByBankAndTermAndRateTypeAndEffectiveDate(
            Bank bank,
            MortgageTerm term,
            RateType rateType,
            LocalDate effectiveDate
    );


    // ========================================================================
    // =============       SENASTE RÄNTOR PER RATE TYPE        =================
    // ========================================================================

    /**
     * Hämtar de senaste räntorna per bank, term och rateType.
     * Använder en subquery för att hämta MAX(effectiveDate).
     */
    @Query("""
        SELECT m
        FROM MortgageRate m
        WHERE m.rateType = :rateType
          AND m.effectiveDate = (
              SELECT MAX(m2.effectiveDate)
              FROM MortgageRate m2
              WHERE m2.bank = m.bank
                AND m2.term = m.term
                AND m2.rateType = :rateType
          )
        """)
    List<MortgageRate> findLatestRatesByType(
            @Param("rateType") RateType rateType
    );
}