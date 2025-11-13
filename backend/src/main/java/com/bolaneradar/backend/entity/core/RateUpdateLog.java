package com.bolaneradar.backend.entity.core;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "rate_update_log")
public class RateUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // När uppdateringen skedde
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    // Källa (t.ex. "ScraperService", "ExampleData")
    @Column(nullable = false)
    private String sourceName;

    // Antal importerade poster
    @Column(nullable = false)
    private int importedCount;

    // Koppling till Bank
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    // === Nya fält för förbättrad loggning ===

    @Column(nullable = false)
    private boolean success = true; // true om scraping lyckades

    @Column(length = 1000)
    private String errorMessage; // felmeddelande om misslyckad scraping

    private long durationMs; // hur lång tid skrapningen tog

    // === Konstruktorer ===
    public RateUpdateLog() {}

    public RateUpdateLog(LocalDateTime occurredAt, String sourceName, int importedCount, Bank bank,
                         boolean success, String errorMessage, long durationMs) {
        this.occurredAt = occurredAt;
        this.sourceName = sourceName;
        this.importedCount = importedCount;
        this.bank = bank;
        this.success = success;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
    }

    // === Getters & setters ===
    public Long getId() { return id; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }

    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}