package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateUpdateLogServiceTest {

    @Mock
    RateUpdateLogRepository rateUpdateLogRepository;

    @InjectMocks
    RateUpdateLogService service;

    // ============================================================
    // logUpdate()
    // ============================================================
    @Test
    void logUpdate_savesLogInRepository() {
        Bank bank = new Bank("Swedbank");

        // Act
        service.logUpdate(bank, "ScraperService", 5, true, null, 1200);

        // Assert
        verify(rateUpdateLogRepository, times(1))
                .save(any(RateUpdateLog.class));
    }

    // ============================================================
    // getAllLogs()
    // ============================================================
    @Test
    void getAllLogs_returnsLogsSorted() {
        Bank bank = new Bank("SEB");
        RateUpdateLog log1 = new RateUpdateLog(LocalDateTime.now(), "Scraper", 3, bank, true, null, 500);
        RateUpdateLog log2 = new RateUpdateLog(LocalDateTime.now().minusHours(1), "Scraper", 1, bank, true, null, 300);

        when(rateUpdateLogRepository.findAllByOrderByOccurredAtDesc())
                .thenReturn(List.of(log1, log2));

        List<RateUpdateLog> result = service.getAllLogs();

        assertEquals(2, result.size());
        assertEquals(log1, result.get(0));
        verify(rateUpdateLogRepository).findAllByOrderByOccurredAtDesc();
    }

    // ============================================================
    // getLogsForBank()
    // ============================================================
    @Test
    void getLogsForBank_returnsLogsForSpecificBank() {
        Bank bank = new Bank("Nordea");
        RateUpdateLog log = new RateUpdateLog(LocalDateTime.now(), "Manual", 2, bank, true, null, 100);

        when(rateUpdateLogRepository.findByBankOrderByOccurredAtDesc(bank))
                .thenReturn(List.of(log));

        List<RateUpdateLog> result = service.getLogsForBank(bank);

        assertEquals(1, result.size());
        assertEquals("Nordea", result.get(0).getBank().getName());
    }

    // ============================================================
    // getLatestLogsPerBank()
    // ============================================================
    @Test
    void getLatestLogsPerBank_returnsLatestPerBank() {
        Bank bank1 = new Bank("Swedbank");
        Bank bank2 = new Bank("SEB");

        RateUpdateLog oldSwedbank = new RateUpdateLog(
                LocalDateTime.now().minusHours(5), "Scraper", 2, bank1, true, null, 900
        );
        RateUpdateLog newSwedbank = new RateUpdateLog(
                LocalDateTime.now(), "Scraper", 3, bank1, true, null, 1000
        );

        RateUpdateLog sebLog = new RateUpdateLog(
                LocalDateTime.now().minusMinutes(10), "Manual", 1, bank2, true, null, 300
        );

        // Repo returnerar alla loggar i DESC order
        when(rateUpdateLogRepository.findAllByOrderByOccurredAtDesc())
                .thenReturn(List.of(newSwedbank, sebLog, oldSwedbank));

        List<RateUpdateLog> result = service.getLatestLogsPerBank();

        assertEquals(2, result.size());
        assertEquals("Swedbank", result.get(0).getBank().getName());
        assertEquals(newSwedbank, result.get(0));
        assertEquals(sebLog, result.get(1));
    }

    // ============================================================
    // getLatestGlobalUpdate()
    // ============================================================
    @Test
    void getLatestGlobalUpdate_returnsNewestTimestamp() {
        Bank bank1 = new Bank("Swedbank");

        RateUpdateLog log1 = new RateUpdateLog(
                LocalDateTime.now().minusHours(1), "Scraper", 2, bank1, true, null, 900
        );
        RateUpdateLog log2 = new RateUpdateLog(
                LocalDateTime.now(), "Scraper", 3, bank1, true, null, 1000
        );

        when(rateUpdateLogRepository.findAllByOrderByOccurredAtDesc())
                .thenReturn(List.of(log2, log1));

        LocalDateTime result = service.getLatestGlobalUpdate();

        assertEquals(log2.getOccurredAt(), result);
    }

    // ============================================================
    // clearAllLogs()
    // ============================================================
    @Test
    void clearAllLogs_callsRepositoryDeleteAll() {
        service.clearAllLogs();
        verify(rateUpdateLogRepository, times(1)).deleteAll();
    }
}