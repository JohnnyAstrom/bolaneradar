package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Exempel på egen metod – vi kan lägga till fler senare.
     * Den här låter oss hitta en bank via dess namn.
     */
    Bank findByName(String name);
}