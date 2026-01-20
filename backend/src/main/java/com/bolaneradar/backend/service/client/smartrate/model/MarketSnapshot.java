package com.bolaneradar.backend.service.client.smartrate.model;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ================================================================
 * MARKET SNAPSHOT
 * ================================================================
 * <p>
 * Representerar ett fryst marknadsläge för snitträntor
 * vid ett givet tillfälle under en SmartRate-analys.
 * <p>
 * Innehåll:
 * - Bästa ränta per bindningstid (lägsta på marknaden)
 * - Medianränta per bindningstid
 * - Bankens egen snittränta per bindningstid
 * <p>
 * Användning:
 * - Byggs en gång per analys i SmartRateMarketDataService
 * - Skickas vidare till analyslogiken för jämförelser
 * <p>
 * Designprinciper:
 * - Immutable värdeobjekt (record)
 * - Innehåller endast färdigberäknad data
 * ================================================================
 */
public record MarketSnapshot(
        Map<MortgageTerm, BigDecimal> bestByTerm,
        Map<MortgageTerm, BigDecimal> medianByTerm,
        Map<MortgageTerm, BigDecimal> bankAvgByTerm
) {}