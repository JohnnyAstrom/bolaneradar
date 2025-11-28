/**
 * bankApi.ts
 *
 * Funktioner för att hämta bankspecifika räntor
 * och historisk snittränta (för grafer).
 */

import { apiGet } from "./client";

/* ============================================================
 *  TYPER FÖR AKTUELLA RÄNTOR (TABELLEN)
 * ============================================================
 */

/** En rad i bankens räntetabell. */
export interface BankRateRow {
    term: string;
    currentRate: number | null;
    change: number | null;
    avgRate: number | null;
    lastChanged: string | null;
}

/** Hela svaret från /rates-endpointen. */
export interface BankRateResponse {
    month: string | null;
    monthFormatted: string | null;
    rows: BankRateRow[];
}

/** Hämtar bankens aktuella räntor (tabellen). */
export async function getBankRates(bankName: string) {
    return apiGet<BankRateResponse>(`/api/bank/${bankName}/rates`);
}

/* ============================================================
 *  TYPER + FUNKTIONER FÖR HISTORISK RÄNTEGRAF
 * ============================================================
 */

/**
 * Backend returnerar:
 *   {
 *      "month": "2025-01-01",
 *      "avgRate": 3.39
 *   }
 *
 * Vi mappar det senare i React till:
 *   { effectiveDate, ratePercent }
 */
export interface HistoricalPoint {
    month: string;      // ISO-datum från backend
    avgRate: number;    // Snittränta från backend
}

/** Hämtar bindningstider med minst 10 datapunkter. */
export async function fetchAvailableTerms(bankName: string) {
    return apiGet<string[]>(`/api/bank/${bankName}/history/available-terms`);
}

/**
 * Hämtar historisk snittränta för en term.
 *
 * Viktigt:
 *   Backend använder nu:
 *     /api/bank/{bankName}/history/data?term=TERM
 */
export async function fetchHistoricalRates(bankName: string, term: string) {
    return apiGet<HistoricalPoint[]>(
        `/api/bank/${bankName}/history/data?term=${term}`
    );
}