package com.bolaneradar.backend.repository;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.RateUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateUpdateLogRepository extends JpaRepository<RateUpdateLog, Long> {
    List<RateUpdateLog> findAllByOrderByOccurredAtDesc();
    List<RateUpdateLog> findByBankOrderByOccurredAtDesc(Bank bank);
}
