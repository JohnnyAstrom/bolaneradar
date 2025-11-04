package com.bolaneradar.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Representerar en specifik bolåneränta kopplad till en bank.
 * Exempel: Swedbank, 3 års bunden ränta, 4.25 %, giltig från 2025-03-01.
 */
@Entity
@Table(name = "mortgage_rates")
public class MortgageRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Räntan tillhör en bank.
     * Foreign key skapas automatiskt (bank_id i tabellen).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    @JsonBackReference
    private Bank bank;

    /**
     * Bindningstiden (t.ex. FIXED_3Y eller VARIABLE_3M).
     * Sparas som text i databasen tack vare EnumType.STRING.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MortgageTerm term;

    /**
     * Typ av ränta (listränta/snittränta)
     * Sparas som text i databasen tack vare EnumType.STRING.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RateType rateType;

    /**
     * Själva räntesatsen i procent, t.ex. 4.85.
     * NUMERIC(4,2) innebär max 99.99 med två decimaler.
     */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal ratePercent;

    /**
     * Datumet då räntan började gälla.
     */
    @Column(nullable = false)
    private LocalDate effectiveDate;

    /**
     * Senaste förändringen i procent jämfört med föregående värde (t.ex. -0.20).
     * Kan sättas manuellt eller beräknas automatiskt vid scraping.
     */
    @Column(precision = 4, scale = 2)
    private BigDecimal rateChange;

    /**
     * Datumet då räntan senast ändrades.
     * Kan sättas manuellt eller uppdateras automatiskt via scraping.
     */
    private LocalDate lastChangedDate;

    // 🔹 Standardkonstruktör krävs av JPA
    public MortgageRate() {}

    // 🔹 Praktisk konstruktör för enklare instansiering
    public MortgageRate(Bank bank, MortgageTerm term, RateType rateType,
                        BigDecimal ratePercent, LocalDate effectiveDate) {
        this.bank = bank;
        this.term = term;
        this.rateType = rateType;
        this.ratePercent = ratePercent;
        this.effectiveDate = effectiveDate;
    }

    // 🔹 Getters & setters
    public Long getId() { return id; }

    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }

    public MortgageTerm getTerm() { return term; }
    public void setTerm(MortgageTerm term) { this.term = term; }

    public RateType getRateType() { return rateType; }
    public void setRateType(RateType rateType) { this.rateType = rateType; }

    public BigDecimal getRatePercent() { return ratePercent; }
    public void setRatePercent(BigDecimal ratePercent) { this.ratePercent = ratePercent; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getRateChange() { return rateChange; }
    public void setRateChange(BigDecimal rateChange) { this.rateChange = rateChange; }

    public LocalDate getLastChangedDate() { return lastChangedDate; }
    public void setLastChangedDate(LocalDate lastChangedDate) { this.lastChangedDate = lastChangedDate; }
}