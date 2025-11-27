/**
 * bankApi.ts
 *
 * Denna fil innehåller funktionen som hämtar
 * bankspecifika bolåneräntor från backend.
 *
 * API-endpoint:
 *   GET /api/bank/{bankName}/rates
 *
 * Response-formatet innehåller:
 *   - month:           Den senaste månad som banken har snittränta för (ISO-format)
 *   - monthFormatted:  Samma månad men formaterad för visning, t.ex. "okt 2025"
 *   - rows:            En lista med alla bindningstider och deras räntor
 *
 * Detta API används på banksidan (BankPage) för att fylla tabellen
 * "Aktuella bolåneräntor".
 */

import { apiGet } from "./client";

/**
 * En rad i bankens räntetabell.
 * Motsvarar exakt det backend returnerar per bindningstid.
 */
export interface BankRateRow {
    term: string;               // Ex: "3 mån"
    currentRate: number | null; // Senaste listränta eller null
    change: number | null;      // Räntans förändring (%) eller null
    avgRate: number | null;     // Snitträntan för banken den aktuella månaden
    lastChanged: string | null; // Datum då listräntan ändrades
}

/**
 * Hela svaret från API:t.
 * Innehåller metadata + tabellrader.
 */
export interface BankRateResponse {
    month: string | null;          // Ex: "2025-10-01"
    monthFormatted: string | null; // Ex: "okt 2025" för visning
    rows: BankRateRow[];           // Alla rader som ska visas i tabellen
}

/**
 * Hämtar räntedata för en specifik bank.
 *
 * Exempel:
 *   const data = await getBankRates("Swedbank");
 *
 * Returnerar BankRateResponse som frontend kan använda direkt i tabellen.
 */
export async function getBankRates(bankName: string) {
    return apiGet<BankRateResponse>(`/api/bank/${bankName}/rates`);
}
