package com.bolaneradar.backend.entity.analytics;

import java.time.LocalDate;

/**
 * Domänmodell som representerar en trend i bolåneräntor mellan två mättillfällen.
 * Används internt i analyslagret för att representera förändringar innan DTO-konvertering.
 */
public class RateTrend {

    private final String bankName;
    private final String term;
    private final String rateType;
    private final double previousRate;
    private final double currentRate;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final double change;

    public RateTrend(String bankName, String term, String rateType,
                     double previousRate, double currentRate,
                     LocalDate fromDate, LocalDate toDate) {
        this.bankName = bankName;
        this.term = term;
        this.rateType = rateType;
        this.previousRate = previousRate;
        this.currentRate = currentRate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.change = Math.round((currentRate - previousRate) * 100.0) / 100.0;
    }

    // Getters
    public String getBankName() { return bankName; }
    public String getTerm() { return term; }
    public String getRateType() { return rateType; }
    public double getPreviousRate() { return previousRate; }
    public double getCurrentRate() { return currentRate; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public double getChange() { return change; }
}