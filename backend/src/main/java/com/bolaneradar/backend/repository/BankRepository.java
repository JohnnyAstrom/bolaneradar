package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Hanterar databasoperationer för Bank.
 * JpaRepository ger oss färdiga metoder som:
 * - findAll()
 * - findById()
 * - save()
 * - deleteById()
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