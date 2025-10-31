package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service som hanterar loggning av räntedataimporter.
 * Används av ScraperService för att registrera när och hur många räntor
 * som importerades för varje bank.
 */
@Service
public class RateUpdateService {

    private final RateUpdateLogRepository rateUpdateLogRepository;

    public RateUpdateService(RateUpdateLogRepository rateUpdateLogRepository) {
        this.rateUpdateLogRepository = rateUpdateLogRepository;
    }

    /**
     * Skapar en ny loggpost för en bank när dess räntor uppdateras.
     *
     * @param bank den bank vars räntor uppdaterades
     * @param sourceName varifrån uppdateringen kom (t.ex. "ScraperService" eller "ManualImport")
     * @param importedCount hur många räntor som importerades
     */
    @Transactional
    public void logUpdate(Bank bank, String sourceName, int importedCount) {
        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                sourceName,
                importedCount,
                bank
        );
        rateUpdateLogRepository.save(log);
    }

    /**
     * Hämtar alla loggar, sorterade efter datum (senaste först).
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getAllLogs() {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        // Ladda in banknamn manuellt för att undvika LazyInitializationException
        logs.forEach(log -> {
            if (log.getBank() != null) {
                log.getBank().getName(); // tvingar fram lazy load
            }
        });

        return logs;
    }

    /**
     * Hämtar loggar för en specifik bank, sorterade efter datum (senaste först).
     *
     * @param bank den bank vars loggar ska hämtas
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getLogsForBank(Bank bank) {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findByBankOrderByOccurredAtDesc(bank);

        logs.forEach(log -> {
            if (log.getBank() != null) {
                log.getBank().getName(); // tvingar fram lazy load
            }
        });

        return logs;
    }

    @Transactional
    public void clearAllLogs() {
        rateUpdateLogRepository.deleteAll();
        System.out.println("Alla uppdateringsloggar borttagna.");
    }
}