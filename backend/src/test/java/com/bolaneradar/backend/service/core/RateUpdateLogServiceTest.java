package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.RateUpdateLog;
import com.bolaneradar.backend.repository.RateUpdateLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för RateUpdateLogService.
 * <p>
 * Fokus:
 *  - Testa skapande av loggar
 *  - Testa hämtning och sortering av loggar
 *  - Testa repository-interaktioner utan riktig databas
 */
@ExtendWith(MockitoExtension.class)
class RateUpdateLogServiceTest {

    @Mock
    RateUpdateLogRepository rateUpdateLogRepository;

    @InjectMocks
    RateUpdateLogService rateUpdateLogService;

    // ============================================================
    // logUpdate()
    // ============================================================
    @Test
    void logUpdate_savesLogWithCorrectFields() {
        // Arrange
        Bank bank = new Bank("SEB");
        String source = "ScraperService";

        // Dummy log capturing
        ArgumentCaptor<RateUpdateLog> captor = ArgumentCaptor.forClass(RateUpdateLog.class);

        // Act
        rateUpdateLogService.logUpdate(bank, source, 3, true, null, 150);

        // Assert
        verify(rateUpdateLogRepository).save(captor.capture());
        RateUpdateLog saved = captor.getValue();

        assertEquals("ScraperService", saved.getSourceName());
        assertEquals(3, saved.getImportedCount());
        assertTrue(saved.isSuccess());
        assertEquals(150, saved.getDurationMs());
        assertEquals(bank, saved.getBank());
        assertNotNull(saved.getOccurredAt());  // sätts i service
    }

    // ============================================================
    // getAllLogs()
    // ============================================================
    @Test
    void getAllLogs_returnsLogsFromRepository_andTriggersLazyLoading() {
        // Arrange
        Bank bank = mock(Bank.class);
        when(bank.getName()).thenReturn("Swedbank");

        RateUpdateLog log = new RateUpdateLog(
                LocalDateTime.now(), "TestSource", 2, bank, true, null, 50
        );

        when(rateUpdateLogRepository.findAllByOrderByOccurredAtDesc())
                .thenReturn(List.of(log));

        // Act
        List<RateUpdateLog> result = rateUpdateLogService.getAllLogs();

        // Assert
        assertEquals(1, result.size());
        assertEquals("TestSource", result.getFirst().getSourceName());
        verify(bank, times(1)).getName(); // lazy-load workaround
    }

    // ============================================================
    // getLogsForBank()
    // ============================================================
    @Test
    void getLogsForBank_returnsLogsForSpecificBank() {
        // Arrange
        Bank bank = new Bank("Nordea");
        RateUpdateLog log1 = new RateUpdateLog(LocalDateTime.now(), "SourceA", 1, bank, true, null, 20);
        RateUpdateLog log2 = new RateUpdateLog(LocalDateTime.now(), "SourceB", 2, bank, false, "Error", 40);

        bank.setId(5L);

        when(rateUpdateLogRepository.findByBankOrderByOccurredAtDesc(bank))
                .thenReturn(List.of(log1, log2));

        // Act
        List<RateUpdateLog> result = rateUpdateLogService.getLogsForBank(bank);

        // Assert
        assertEquals(2, result.size());
        assertEquals("SourceA", result.get(0).getSourceName());
        assertEquals("SourceB", result.get(1).getSourceName());
        assertEquals(bank, result.get(0).getBank());
    }

    // ============================================================
    // getLatestLogsPerBank()
    // ============================================================
    @Test
    void getLatestLogsPerBank_returnsLatestLogForEachBank_sortedByDateDesc() {
        // Arrange
        Bank seb = new Bank("SEB");
        Bank nordea = new Bank("Nordea");

        RateUpdateLog logSebNewest = new RateUpdateLog(
                LocalDateTime.now().plusHours(2), "Scraper", 3, seb, true, null, 10
        );
        RateUpdateLog logSebOlder = new RateUpdateLog(
                LocalDateTime.now().minusHours(5), "Scraper", 1, seb, true, null, 20
        );

        RateUpdateLog logNordeaNewest = new RateUpdateLog(
                LocalDateTime.now().plusHours(1), "Manual", 2, nordea, true, null, 15
        );

        when(rateUpdateLogRepository.findAllByOrderByOccurredAtDesc())
                .thenReturn(List.of(logSebNewest, logSebOlder, logNordeaNewest));

        // Act
        List<RateUpdateLog> result = rateUpdateLogService.getLatestLogsPerBank();

        // Assert
        assertEquals(2, result.size());
        assertEquals("SEB", result.get(0).getBank().getName()); // newest overall first
        assertEquals("Nordea", result.get(1).getBank().getName());
    }

    // ============================================================
    // clearAllLogs()
    // ============================================================
    @Test
    void clearAllLogs_callsDeleteAll() {
        // Act
        rateUpdateLogService.clearAllLogs();

        // Assert
        verify(rateUpdateLogRepository, times(1)).deleteAll();
    }
}