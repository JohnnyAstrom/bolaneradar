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
import i18n from "../i18n";

function getLanguageParam() {
    return i18n.language === "en" ? "EN" : "SV";
}

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
    const language = getLanguageParam();

    return apiGet<BankDetails>(
        `/api/banks/${bankKey}/details?language=${language}`
    );
}



/** Hämtar bankens introduktionssektion (beskrivning, USP, CTA). */
export async function getBankIntro(bankKey: string) {
    const language = getLanguageParam();

    return apiGet<BankIntro>(
        `/api/banks/${bankKey}/intro?language=${language}`
    );
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
    return apiGet<BankRateResponse>(`/api/banks/${bankName}/rates`);
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
export async function fetchAvailableTerms(bankKey: string) {
    return apiGet<string[]>(`/api/banks/${bankKey}/history/available-terms`);
}

/** Hämtar historiska snitträntor för en viss term. */
export async function fetchHistoricalRates(bankKey: string, term: string) {
    return apiGet<HistoricalPoint[]>(
        `/api/banks/${bankKey}/history/data?term=${term}`
    );
}

/* ============================================================
 *  BANK INFO – FÖRDJUPAD INFOSIDA
 * ============================================================
 */

export interface BankInfo {
    intro: string;
    deepInsights: { heading: string; text: string }[];
    faq: { question: string; answer: string }[];
    ctaLabel: string;
    ctaUrl: string;
}


/** Hämtar fördjupad information om banken (t.ex. lång text till infosidan) */
export async function getBankInfo(bankKey: string) {
    const language = i18n.language === "en" ? "EN" : "SV";

    return apiGet<BankInfo>(
        `/api/banks/${bankKey}/info?language=${language}`
    );
}