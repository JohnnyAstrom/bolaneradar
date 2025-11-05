package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.RateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Hanterar databasoperationer för MortgageRate.
 */
@Repository
public interface MortgageRateRepository extends JpaRepository<MortgageRate, Long> {

    List<MortgageRate> findByBank(Bank bank);

    void deleteByBank(Bank bank);

    List<MortgageRate> findByEffectiveDate(LocalDate effectiveDate);


    List<MortgageRate> findByEffectiveDateBetween(LocalDate from, LocalDate to);

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

    List<MortgageRate> findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
            Bank bank,
            com.bolaneradar.backend.model.MortgageTerm term,
            com.bolaneradar.backend.model.RateType rateType
    );

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

    List<MortgageRate> findByBankAndRateTypeAndEffectiveDate(
            Bank bank,
            RateType rateType,
            LocalDate effectiveDate
    );

    /**
     * Hämta alla unika datum (senaste först) där räntor finns registrerade.
     * Används när from/to saknas och vi vill jämföra globalt (t.ex. LISTRATE).
     */
    @Query("SELECT DISTINCT m.effectiveDate FROM MortgageRate m ORDER BY m.effectiveDate DESC")
    List<LocalDate> findDistinctEffectiveDatesDesc();

    /**
     * Hämta alla räntor för en viss räntetyp på ett specifikt datum.
     * Används när from/to anges tillsammans med rateType.
     */
    List<MortgageRate> findByRateTypeAndEffectiveDate(RateType rateType, LocalDate effectiveDate);
}