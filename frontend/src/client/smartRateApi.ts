import { apiPost } from "./client";
import type { SmartRateTestRequest, SmartRateTestResult } from "../types/smartRate";

/**
 * Kör Smart Räntetest baserat på användarens input.
 *
 * Requestformat:
 * {
 *   bank: string;
 *   hasOffer: boolean;
 *   currentRate?: number;
 *   currentRateTerm?: string;
 *   rateChangeDate?: string;
 *   bindingEndDate?: string;
 *   futureRatePreference?: string;
 *   offerBindingTerm?: string;
 *   offerRate?: number;
 *   offerStartDate?: string;
 *   offerComparisonTarget?: string;
 * }
 *
 * Responseformat:
 * {
 *   status: string;
 *   bank: string;
 *   analyzedTerm: string;
 *   differenceFromBankAverage: number;
 *   differenceFromBestMarketAverage: number;
 *   analysisText: string;
 *   additionalContext: string;
 *   recommendation: string;
 * }
 */
export async function runSmartRateTest(payload: SmartRateTestRequest) {
    return apiPost<SmartRateTestResult, SmartRateTestRequest>(
        "/api/smartrate/test",
        payload
    );
}