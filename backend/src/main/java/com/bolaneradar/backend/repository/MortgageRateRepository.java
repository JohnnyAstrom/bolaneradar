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

    /**
     * Hämta alla räntor för en specifik bank.
     */
    List<MortgageRate> findByBank(Bank bank);

    /**
     * Ta bort alla räntor för en specifik bank.
     * Används t.ex. i AdminDataService för att rensa data under test.
     */
    void deleteByBank(Bank bank);

    /**
     * Hämta alla räntor för ett specifikt datum.
     */
    List<MortgageRate> findByEffectiveDate(LocalDate effectiveDate);

    /**
     * Hämta alla unika datum (senaste först) där räntor finns registrerade.
     * Används för att identifiera de två senaste mättillfällena.
     */
    @Query("SELECT DISTINCT m.effectiveDate FROM MortgageRate m ORDER BY m.effectiveDate DESC")
    List<LocalDate> findDistinctEffectiveDatesDesc();

    /**
     * Hämta alla räntor inom ett valt datumintervall (inklusive gränserna).
     * Används av getRateTrendsInRange() för att visa alla ändringar inom en period.
     */
    List<MortgageRate> findByEffectiveDateBetween(LocalDate from, LocalDate to);

    /**
     * Hämtar de senaste räntorna per bank och bindningstid
     * för en specifik räntetyp (LISTRATE eller AVERAGERATE).
     * <p>
     * SQL-frågan använder subquery för att endast hämta de nyaste posterna
     * per (bank, term, rateType).
     */
    @Query("""
        SELECT m FROM MortgageRate m
        WHERE m.rateType = :rateType AND m.effectiveDate = (
            SELECT MAX(m2.effectiveDate)
            FROM MortgageRate m2
            WHERE m2.bank = m.bank
              AND m2.term = m.term
              AND m2.rateType = m.rateType
        )
        ORDER BY m.bank.name, m.term
    """)
    List<MortgageRate> findLatestRatesByType(@Param("rateType") RateType rateType);
}
