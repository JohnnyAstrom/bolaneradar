package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ================================================================
 * RATE UPDATE LOG SERVICE
 * ================================================================
 * <p>
 * Ansvar:
 * - Loggar och tillhandahåller historik för ränteuppdateringar
 * - Används av scraper- och adminflöden
 * <p>
 * Funktionalitet:
 * - Sparar metadata om varje uppdateringskörning (tid, bank, status)
 * - Möjliggör vyer som ”senast uppdaterad per bank”
 * <p>
 * Designprinciper:
 * - Arbetar enbart med entiteter
 * - Ingen DTO-mappning eller affärslogik
 * <p>
 * Användningsområde:
 * - Drift, övervakning och felsökning
 * - Ej avsedd för publik frontend
 * ================================================================
 */
@Service
public class RateUpdateLogService {

    private final RateUpdateLogRepository rateUpdateLogRepository;

    public RateUpdateLogService(RateUpdateLogRepository rateUpdateLogRepository) {
        this.rateUpdateLogRepository = rateUpdateLogRepository;
    }

    // ===========================================================
    // ==================     CREATE LOG     =====================
    // ===========================================================

    /**
     * Skapar en ny loggpost för en bank när dess räntor uppdateras.
     *
     * @param bank          Banken som uppdaterades (kan vara null).
     * @param sourceName    Källa till uppdateringen (t.ex. "ScraperService" eller "ManualImport").
     * @param importedCount Antal räntor som importerades.
     * @param success       Om uppdateringen lyckades.
     * @param errorMessage  Felmeddelande (om något gick fel, annars null).
     * @param durationMs    Hur lång tid uppdateringen tog i millisekunder.
     */
    @Transactional
    public void logUpdate(Bank bank, String sourceName, int importedCount,
                          boolean success, String errorMessage, long durationMs) {

        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(),
                sourceName,
                importedCount,
                bank,
                success,
                errorMessage,
                durationMs
        );

        rateUpdateLogRepository.save(log);
    }

    // ===========================================================
    // ===================     READ LOGS     =====================
    // ===========================================================

    /**
     * Hämtar alla loggar sorterade efter tidpunkt (senaste först).
     *
     * @return Lista med RateUpdateLog-entiteter.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getAllLogs() {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        // Ladda bankens namn manuellt för att undvika LazyInitializationException
        logs.forEach(log -> {
            if (log.getBank() != null) log.getBank().getName();
        });

        return logs;
    }

    /**
     * Hämtar alla loggar för en specifik bank (senaste först).
     *
     * @param bank Den bank vars loggar ska hämtas.
     * @return Lista med RateUpdateLog-entiteter.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getLogsForBank(Bank bank) {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findByBankOrderByOccurredAtDesc(bank);

        logs.forEach(log -> {
            if (log.getBank() != null) log.getBank().getName();
        });

        return logs;
    }

    /**
     * Hämtar den senaste uppdateringsloggen per bank.
     * Används t.ex. för att visa "Senast uppdaterad" i gränssnittet.
     *
     * @return En lista med den senaste loggposten för varje bank.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getLatestLogsPerBank() {
        List<RateUpdateLog> allLogs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        Map<String, RateUpdateLog> latestPerBank = allLogs.stream()
                .filter(log -> log.getBank() != null)
                .collect(Collectors.toMap(
                        log -> log.getBank().getName(),
                        log -> log,
                        (existing, replacement) -> existing // behåll första (nyaste)
                ));

        return latestPerBank.values().stream()
                .sorted(Comparator.comparing(RateUpdateLog::getOccurredAt).reversed())
                .toList();
    }

    public LocalDateTime getLatestGlobalUpdate() {
        var latestPerBank = getLatestLogsPerBank();

        return latestPerBank.stream()
                .map(RateUpdateLog::getOccurredAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }




    // ===========================================================
    // ===================     DELETE LOGS     ===================
    // ===========================================================

    /**
     * Tar bort alla loggar från databasen.
     * Kan användas för att nollställa historiken.
     */
    @Transactional
    public void clearAllLogs() {
        rateUpdateLogRepository.deleteAll();
        System.out.println("Alla uppdateringsloggar borttagna.");
    }
}