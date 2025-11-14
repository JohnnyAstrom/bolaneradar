package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Hanterar databasoperationer för MortgageRate.
 *
 * Repository-lagret ansvarar för alla direkta SQL-frågor mot tabellen
 * 'mortgage_rate'. Här använder vi både automatiskt genererade frågor enligt
 * Spring Data JPA:s namngivningsregler samt egna @Query för mer avancerade behov.
 */
@Repository
public interface MortgageRateRepository extends JpaRepository<MortgageRate, Long> {

    /**
     * Hämta alla räntor för en viss bank.
     */
    List<MortgageRate> findByBank(Bank bank);

    /**
     * Ta bort alla räntor kopplade till en bank.
     * Används exempelvis vid fullständig omimport av historiska data.
     */
    void deleteByBank(Bank bank);

    /**
     * Hämta alla räntor som gäller exakt ett specifikt datum (för alla banker och termer).
     */
    List<MortgageRate> findByEffectiveDate(LocalDate effectiveDate);

    /**
     * Hämta alla räntor i ett intervall av effektiva datum.
     * Exempel: lista snitträntor mellan januari och april 2024.
     */
    List<MortgageRate> findByEffectiveDateBetween(LocalDate from, LocalDate to);

    /**
     * Hämta de senaste räntorna per bank, term och rateType.
     * Detta fungerar genom en subquery som väljer MAX(effectiveDate).
     *
     * Exempel:
     * - Alla senaste listräntor (LISTRATE)
     * - Alla senaste snitträntor (AVERAGERATE)
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
    List<MortgageRate> findLatestRatesByType(@Param("rateType") RateType rateType);

    /**
     * Hämta alla räntor för en specifik kombination av:
     * - bank
     * - bolånetyp (term)
     * - rateType (LISTRATE eller AVERAGERATE)
     *
     * Sorteras i fallande datumordning så att index 0 alltid är senaste värdet.
     */
    List<MortgageRate> findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
            Bank bank,
            MortgageTerm term,
            RateType rateType
    );

    /**
     * Hämta alla unika effektiva datum för en viss bank + rateType.
     * Sorteras senaste först.
     *
     * Exempel: alla månader där snitträntor finns för Handelsbanken.
     */
    @Query("""
        SELECT DISTINCT m.effectiveDate
        FROM MortgageRate m
        WHERE m.bank = :bank AND m.rateType = :rateType
        ORDER BY m.effectiveDate DESC
        """)
    List<LocalDate> findDistinctEffectiveDatesByBankAndRateTypeDesc(
            @Param("bank") Bank bank,
            @Param("rateType") RateType rateType
    );

    /**
     * Hämta alla räntor för en bank + rateType + ett specifikt datum.
     * Används främst vid visning av snitträntor månad för månad.
     */
    List<MortgageRate> findByBankAndRateTypeAndEffectiveDate(
            Bank bank,
            RateType rateType,
            LocalDate effectiveDate
    );

    /**
     * Hämta alla unika datum där det finns någon ränta överhuvudtaget i systemet.
     * Sorteras senaste först.
     */
    @Query("SELECT DISTINCT m.effectiveDate FROM MortgageRate m ORDER BY m.effectiveDate DESC")
    List<LocalDate> findDistinctEffectiveDatesDesc();

    /**
     * Hämta alla räntor av en viss typ (LISTRATE/AVERAGERATE) för ett visst datum.
     * Exempelvis vid analys eller jämförelser mellan banker.
     */
    List<MortgageRate> findByRateTypeAndEffectiveDate(RateType rateType, LocalDate effectiveDate);

    /**
     * Kontrollera om exakt samma snittränta redan finns i databasen.
     *
     * Detta används för att undvika att AVERAGERATE (snitträntor)
     * sparas flera gånger för samma:
     * - bank
     * - term (t.ex. 3 år)
     * - rateType
     * - effectiveDate (månadens första datum)
     *
     * Listräntor (LISTRATE) ska däremot INTE blockeras av denna metod.
     */
    boolean existsByBankAndTermAndRateTypeAndEffectiveDate(
            Bank bank,
            MortgageTerm term,
            RateType rateType,
            LocalDate effectiveDate
    );
}