package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
