package com.bolaneradar.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mortgage_rates")
public class MortgageRate {

    /**
     * Enum som listar de bindningstider vi vill stödja.
     * Bara dessa värden är giltiga i databasen.
     */
    public enum RateTerm {
        VARIABLE_3M,   // rörlig ränta (3 månader)
        FIXED_1Y,      // bunden 1 år
        FIXED_2Y,      // bunden 2 år
        FIXED_3Y,      // bunden 3 år
        FIXED_5Y       // bunden 5 år
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Varje ränta hör till en bank.
     * "bank_id" blir foreign key i tabellen mortgage_rates.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Bank bank;


    /**
     * Bindningstid – sparas som text (ex. "FIXED_3Y").
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RateTerm term;

    /**
     * Själva räntesatsen, t.ex. 4.85 %.
     * NUMERIC(4,2) = max 99.99, två decimaler.
     */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal ratePercent;

    /**
     * Datum då räntan började gälla.
     */
    @Column(nullable = false)
    private LocalDate effectiveDate;

    public MortgageRate() {}

    public MortgageRate(Bank bank, RateTerm term, BigDecimal ratePercent, LocalDate effectiveDate) {
        this.bank = bank;
        this.term = term;
        this.ratePercent = ratePercent;
        this.effectiveDate = effectiveDate;
    }

    // Getters & setters
    public Long getId() { return id; }

    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }

    public RateTerm getTerm() { return term; }
    public void setTerm(RateTerm term) { this.term = term; }

    public BigDecimal getRatePercent() { return ratePercent; }
    public void setRatePercent(BigDecimal ratePercent) { this.ratePercent = ratePercent; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
