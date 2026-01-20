package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ================================================================
 * RATEUPDATELOGREPOSITORY
 * ================================================================
 * Detta lager hanterar:
 * - Databasåtkomst för RateUpdateLog
 * - Hämtning av uppdateringshistorik per bank eller globalt
 * <p></p>
 * Repository-lagret ska:
 * - Vara helt fritt från affärslogik
 * - Endast tillhandahålla sorterad och filtrerad rådata
 *   till service-lagret
 * ================================================================
 */
@Repository
public interface RateUpdateLogRepository extends JpaRepository<RateUpdateLog, Long> {
    List<RateUpdateLog> findAllByOrderByOccurredAtDesc();
    List<RateUpdateLog> findByBankOrderByOccurredAtDesc(Bank bank);
}
