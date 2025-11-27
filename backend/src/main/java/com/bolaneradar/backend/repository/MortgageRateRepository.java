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
    // ==========    SENASTE RÄNTA FÖR BANK + TERM + RATE TYPE    ============
    // ========================================================================

    /**
     * Hämtar den senaste (högsta effectiveDate) räntan
     * för en specifik bank + bindningstid (term) + rateType (LISTRATE/AVERAGERATE).
     *
     * Spring Data JPA genererar automatiskt SQL:
     *   SELECT ... ORDER BY effective_date DESC LIMIT 1
     *
     * Om banken inte har någon ränta för kombinationen returneras null.
     */
    MortgageRate findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
            Long bankId,
            MortgageTerm term,
            RateType rateType
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

    // ========================================================================
    // ==========   GEMENSAM SNITTRÄNTA-MÅNAD FÖR ALLA BANKER   ===============
    // ========================================================================

    /**
     * Hämtar den senaste gemensamma snitträntemånaden (AVERAGERATE) för alla banker.
     *
     * Logiken:
     *   - För varje bank hämtas den senaste snitträntan (MAX(effectiveDate))
     *   - Eftersom alla banker publicerar snitträntor månadsvis
     *     väljer vi den MINSTA av dessa datum.
     *
     * Resultatet är den månad som alla banker hunnit redovisa.
     * Används bl.a. för att sätta rubriken "Snittränta (oktober 2025)".
     *
     * Returnerar:
     *   - LocalDate (ex. 2025-10-01) om alla banker har data
     *   - null om snitträntor saknas
     */
    @Query("""
    SELECT MIN(latest_date) FROM (
        SELECT MAX(m.effectiveDate) AS latest_date
        FROM MortgageRate m
        WHERE m.rateType = 'AVERAGERATE'
          AND m.term = :term
        GROUP BY m.bank.id
    )
    """)
        LocalDate findCommonEffectiveDateForAverageRates(@Param("term") MortgageTerm term);

    // ========================================================================
// ==============     BANKENS SENASTE SNITTRÄNTA-MÅNAD     =================
// ========================================================================

    /**
     * Hämtar den senaste snitträntan (AVERAGERATE) för en specifik bank.
     *
     * Logik:
     *  - Varje snittränta har ett effectiveDate (t.ex. 2025-10-01)
     *  - Vi tar MAX(effectiveDate) för banken
     *
     * Resultat:
     *  - Returnerar t.ex. 2025-10-01 för Swedbank
     *  - Används av Bank-sidan för att visa "Snittränta (okt 2025)"
     *
     * Returnerar:
     *  - LocalDate (senaste datumet)
     *  - null om banken inte har några snitträntor alls
     */
    @Query("""
    SELECT MAX(m.effectiveDate)
    FROM MortgageRate m
    WHERE m.bank.id = :bankId
      AND m.rateType = 'AVERAGERATE'
""")
    LocalDate findLatestAverageDateForBank(@Param("bankId") Long bankId);


// ========================================================================
// ===========  SNITTRÄNTA FÖR BANK + TERM INOM EN SPECIFIK MÅNAD  =========
// ========================================================================

    /**
     * Hämtar snittränta (AVERAGERATE) för en bank och en viss bindningstid,
     * inom en given månad.
     *
     * Logik:
     *  - monthStart = första dagen i månaden (t.ex. 2025-10-01)
     *  - monthEnd   = nästa månads första dag (t.ex. 2025-11-01)
     *
     * Om banken saknar snittränta för just den månaden:
     *  - returneras null (exempel: 9 år saknas i oktober 2025)
     *
     * Används av Bank-sidan för att visa avgRate per term.
     */
    @Query("""
    SELECT m FROM MortgageRate m
    WHERE m.bank.id = :bankId
      AND m.term = :term
      AND m.rateType = 'AVERAGERATE'
      AND m.effectiveDate >= :monthStart
      AND m.effectiveDate < :monthEnd
    ORDER BY m.effectiveDate DESC
    """)
    MortgageRate findAverageRateForBankAndTermAndMonth(
            @Param("bankId") Long bankId,
            @Param("term") MortgageTerm term,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );
}