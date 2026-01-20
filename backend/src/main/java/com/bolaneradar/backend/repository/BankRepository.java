package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.entity.core.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ================================================================
 * BANKREPOSITORY
 * ================================================================
 * Detta lager hanterar:
 * - Alla direkta databasoperationer för Bank
 * - Grundläggande CRUD via Spring Data JPA
 * - Enkla sökfrågor baserade på banknamn
 * <p></p>
 * Repository-lagret ska:
 * - Aldrig innehålla affärslogik
 * - Endast exponera rådata till service-lagret
 * ================================================================
 */
@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    /**
     * Hitta en bank via dess namn (skiftlägeskänsligt).
     */
    Optional<Bank> findByName(String name);

    /**
     * Hitta en bank via namn utan att bry sig om versaler/gemener.
     */
    Optional<Bank> findByNameIgnoreCase(String name);
}