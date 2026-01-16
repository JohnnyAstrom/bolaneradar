package com.bolaneradar.backend.dto.api;

import java.math.BigDecimal;

public class RateUpdateDto {

    private String bankKey;
    private String bankName;

    private String bindingPeriod;

    private BigDecimal previousRate;
    private BigDecimal newRate;

    public RateUpdateDto() {
    }

    public RateUpdateDto(
            String bankKey,
            String bankName,
            String bindingPeriod,
            BigDecimal previousRate,
            BigDecimal newRate
    ) {
        this.bankKey = bankKey;
        this.bankName = bankName;
        this.bindingPeriod = bindingPeriod;
        this.previousRate = previousRate;
        this.newRate = newRate;
    }

    public String getBankKey() {
        return bankKey;
    }

    public void setBankKey(String bankKey) {
        this.bankKey = bankKey;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBindingPeriod() {
        return bindingPeriod;
    }

    public void setBindingPeriod(String bindingPeriod) {
        this.bindingPeriod = bindingPeriod;
    }

    public BigDecimal getPreviousRate() {
        return previousRate;
    }

    public void setPreviousRate(BigDecimal previousRate) {
        this.previousRate = previousRate;
    }

    public BigDecimal getNewRate() {
        return newRate;
    }

    public void setNewRate(BigDecimal newRate) {
        this.newRate = newRate;
    }
}
