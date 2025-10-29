package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service som hanterar loggning av datainsamlingar och uppdateringar.
 * Varje gång data importeras eller uppdateras skapas en loggrad i databasen.
 */
@Service
public class RateUpdateService {

    private final RateUpdateLogRepository logRepository;

    // Konstruktorinjektion – Spring hanterar beroendet automatiskt
    public RateUpdateService(RateUpdateLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Loggar en ny uppdatering till databasen.
     *
     * @param bank den bank uppdateringen gäller (kan vara null om generell)
     * @param sourceName källa för uppdateringen, t.ex. "ExampleData" eller "Swedbank Scraper"
     * @param importedCount hur många poster som importerades/uppdaterades
     * @return den sparade loggposten
     */
    public RateUpdateLog logUpdate(Bank bank, String sourceName, int importedCount) {
        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                sourceName,
                importedCount,
                bank
        );
        return logRepository.save(log);
    }

    /**
     * Hämtar alla loggar (senaste först).
     */
    public List<RateUpdateLog> getAllLogs() {
        return logRepository.findAllByOrderByOccurredAtDesc();
    }

    /**
     * Hämtar loggar för en specifik bank (senaste först).
     */
    public List<RateUpdateLog> getLogsForBank(Bank bank) {
        return logRepository.findByBankOrderByOccurredAtDesc(bank);
    }

    public void deleteAllLogs() {
        logRepository.deleteAll();
    }
}
