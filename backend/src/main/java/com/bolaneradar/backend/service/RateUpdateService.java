package com.bolaneradar.backend.service;

import com.bolaneradar.backend.dto.RateUpdateLogDto;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-klass som hanterar loggning och hämtning av uppdateringar
 * för bolåneräntor. Den används främst av ScraperService för att spara
 * information om när varje bank uppdaterades, hur lång tid det tog,
 * om det lyckades och hur många räntor som importerades.
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
     * @param bank          Den bank vars räntor uppdaterades.
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
                LocalDateTime.now(),   // När loggen skapades
                sourceName,            // Varifrån uppdateringen kom
                importedCount,         // Antal räntor som importerades
                bank,                  // Banken som uppdaterades (kan vara null)
                success,               // Om scraping lyckades eller ej
                errorMessage,          // Felmeddelande vid misslyckande
                durationMs             // Hur lång tid det tog
        );

        rateUpdateLogRepository.save(log);
    }

    /**
     * Hämtar alla loggar sorterade efter tidpunkt (senaste först)
     * och konverterar dem till DTO-objekt som används av API:t.
     *
     * @return Lista med RateUpdateLogDto som innehåller sammanfattad loggdata.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLogDto> getAllLogsAsDto() {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        // Konvertera entiteter till DTO:er
        return logs.stream()
                .map(log -> new RateUpdateLogDto(
                        log.getId(),
                        log.getBank() != null ? log.getBank().getName() : null,
                        log.getSourceName(),
                        log.getImportedCount(),
                        log.isSuccess(),
                        log.getErrorMessage(),
                        log.getDurationMs(),
                        log.getOccurredAt()
                ))
                .toList();
    }

    /**
     * Hämtar alla loggar (utan konvertering till DTO).
     * Används främst internt om man vill ha tillgång till hela entiteten.
     *
     * @return Lista med RateUpdateLog-entiteter.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getAllLogs() {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        // Ladda in banknamn manuellt för att undvika LazyInitializationException
        logs.forEach(log -> {
            if (log.getBank() != null) {
                log.getBank().getName();
            }
        });

        return logs;
    }

    /**
     * Hämtar loggar för en specifik bank, sorterade efter tidpunkt (senaste först).
     *
     * @param bank Den bank vars loggar ska hämtas.
     * @return Lista med loggar för den angivna banken.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLog> getLogsForBank(Bank bank) {
        List<RateUpdateLog> logs = rateUpdateLogRepository.findByBankOrderByOccurredAtDesc(bank);

        logs.forEach(log -> {
            if (log.getBank() != null) {
                log.getBank().getName();
            }
        });

        return logs;
    }

    /**
     * Hämtar den senaste uppdateringen för varje bank.
     * Används t.ex. för att visa "Senast uppdaterad" i gränssnittet.
     *
     * @return En lista med den senaste loggposten per bank, konverterad till DTO.
     */
    @Transactional(readOnly = true)
    public List<RateUpdateLogDto> getLatestUpdatesPerBank() {
        List<RateUpdateLog> allLogs = rateUpdateLogRepository.findAllByOrderByOccurredAtDesc();

        // Gruppera loggar per bank och hämta den senaste per grupp
        Map<String, RateUpdateLog> latestPerBank = allLogs.stream()
                .filter(log -> log.getBank() != null)
                .collect(Collectors.toMap(
                        log -> log.getBank().getName(),
                        log -> log,
                        (existing, replacement) -> existing // behåll första (nyaste)
                ));

        // Konvertera till DTO:er
        return latestPerBank.values().stream()
                .map(log -> new RateUpdateLogDto(
                        log.getId(),
                        log.getBank().getName(),
                        log.getSourceName(),
                        log.getImportedCount(),
                        log.isSuccess(),
                        log.getErrorMessage(),
                        log.getDurationMs(),
                        log.getOccurredAt()
                ))
                .sorted(Comparator.comparing(RateUpdateLogDto::occurredAt).reversed()) // sortera nyast först
                .toList();
    }

    /**
     * Rensar bort alla loggar från databasen.
     * Kan användas för att nollställa historiken.
     */
    @Transactional
    public void clearAllLogs() {
        rateUpdateLogRepository.deleteAll();
        System.out.println("Alla uppdateringsloggar borttagna.");
    }
}