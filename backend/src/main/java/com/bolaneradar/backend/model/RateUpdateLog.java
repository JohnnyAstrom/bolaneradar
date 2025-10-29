package com.bolaneradar.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_update_log")
public class RateUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // När uppdateringen skedde
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    // Valfritt fältnamn för källa (t ex "ExampleData", "SwedbankScraper", "NordeaApi")
    @Column(nullable = false)
    private String sourceName;

    // Hur många poster som importerade/uppdaterades
    @Column(nullable = false)
    private int importedCount;

    // Vilken bank uppdateringen berörde (kan vara null om generell uppdatering)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    public RateUpdateLog() {}

    public RateUpdateLog(LocalDateTime occurredAt, String sourceName, int importedCount, Bank bank) {
        this.occurredAt = occurredAt;
        this.sourceName = sourceName;
        this.importedCount = importedCount;
        this.bank = bank;
    }

    // Getters och setters
    public Long getId() { return id; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }
}
