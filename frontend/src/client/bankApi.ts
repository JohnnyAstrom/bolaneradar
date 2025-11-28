/**
 * bankApi.ts
 *
 * Samlar alla API-anrop som frontend gör för bankdata:
 *  - Aktuella räntor (tabellen)
 *  - Historiska snitträntor (grafer)
 *  - Bankintroduktion (beskrivning + USP)
 *
 * Alla nätverksanrop hålls centralt för att hålla komponenter rena.
 */

import { apiGet } from "./client";

/* ============================================================
 *  BANK INTRO – BESKRIVNING + USP
 * ============================================================
 */

/**
 * Typ för det backend skickar vid:
 *   GET /api/banks/{bankKey}/intro
 */
export interface BankIntro {
    bankKey: string;
    description: string;
    uspItems: string[];
}

/* ============================================================
 *  BANK DETAILS – PASSAR BÄST FÖR / MINDRE BRA FÖR
 * ============================================================
 */

export interface BankDetails {
    description: string;
    overviewText: string;
    bestFor: string[];
    notFor: string[];
    primaryCtaLabel?: string;
    primaryCtaUrl?: string;
    secondaryCtaLabel?: string;
    secondaryCtaUrl?: string;
}

export async function getBankDetails(bankKey: string) {
    return apiGet<BankDetails>(`/api/banks/${bankKey}/details`);
}


/** Hämtar bankens introduktionssektion (beskrivning, USP, CTA). */
export async function getBankIntro(bankKey: string) {
    return apiGet<BankIntro>(`/api/banks/${bankKey}/intro`);
}

/* ============================================================
 *  TYPER FÖR AKTUELLA RÄNTOR (TABELLEN)
 * ============================================================
 */

/** En rad i bankens aktuella räntetabell. */
export interface BankRateRow {
    term: string;                // Ex: "3M", "1Y", "3Y"
    currentRate: number | null;  // Bankens aktuella ränta för termen
    change: number | null;       // Förändring i procentenheter
    avgRate: number | null;      // Snittränta för månaden
    lastChanged: string | null;  // Datum för senaste ändringen
}

/** API-svar för tabellen. */
export interface BankRateResponse {
    month: string | null;          // ISO månad från backend
    monthFormatted: string | null; // Ex: "Okt 2025"
    rows: BankRateRow[];           // Själva tabellraderna
}

/** Hämtar bankens aktuella räntor (tabellen). */
export async function getBankRates(bankName: string) {
    return apiGet<BankRateResponse>(`/api/bank/${bankName}/rates`);
}

/* ============================================================
 *  HISTORISK RÄNTEGRAF – TYPER + API
 * ============================================================
 */

/**
 * En historisk datapunkt från backend.
 * Backend skickar:
 *   {
 *     "month": "2025-01-01",
 *     "avgRate": 3.39
 *   }
 */
export interface HistoricalPoint {
    month: string;     // ISO-datum
    avgRate: number;   // Snittränta
}

/** Lista över bindningstider som har minst 10 datapunkter. */
export async function fetchAvailableTerms(bankName: string) {
    return apiGet<string[]>(`/api/bank/${bankName}/history/available-terms`);
}

/** Hämtar historiska snitträntor för en viss term. */
export async function fetchHistoricalRates(bankName: string, term: string) {
    return apiGet<HistoricalPoint[]>(
        `/api/bank/${bankName}/history/data?term=${term}`
    );
}