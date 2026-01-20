package com.bolaneradar.backend.repository.projection;

import com.bolaneradar.backend.entity.enums.MortgageTerm;

import java.math.BigDecimal;

/**
 * ================================================================
 * MARKET RATE SNAPSHOT PROJECTION
 * ================================================================
 * Projection-interface som används för att hämta ett reducerat
 * urval av fält direkt från databasen.
 * <p>
 * Används av:
 * - SmartRateMarketDataService
 * <p>
 * Syfte:
 * - Möjliggör ett enda, prestandaoptimerat DB-anrop
 * - Undviker att ladda hela MortgageRate-entiteter
 * <p>
 * Innehåll:
 * - Bank-ID
 * - Bindningstid (MortgageTerm)
 * - Snittränta (ratePercent)
 * <p>
 * Designprinciper:
 * - Endast läsning
 * - Ingen logik
 * - Endast för interna beräkningar (ej API/DTO)
 * ================================================================
 */
public interface MarketRateSnapshotRow {
    Long getBankId();

    MortgageTerm getTerm();

    BigDecimal getRatePercent();
}