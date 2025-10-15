package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Hanterar databasoperationer för MortgageRate.
 */
@Repository
public interface MortgageRateRepository extends JpaRepository<MortgageRate, Long> {

    /**
     * Hämta alla räntor för en specifik bank.
     * (Spring genererar SQL automatiskt baserat på metodnamnet.)
     */
    List<MortgageRate> findByBank(Bank bank);
}