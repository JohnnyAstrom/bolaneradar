import { apiGet } from "./client";
import type { MortgageRateComparison } from "../types/mortgage";

/**
 * Hämtar jämförelsedata + snitträntans månad i samma response.
 *
 * Response format:
 * {
 *   averageMonthFormatted: "okt. 2025",
 *   averageMonth: "2025-10-01",
 *   rows: MortgageRateComparison[]
 * }
 */
export async function getComparisonRates(term: string) {
    return apiGet<{
        averageMonthFormatted: string | null;
        averageMonth: string | null;
        rows: MortgageRateComparison[];
    }>(`/api/rates/comparison?term=${term}`);
}