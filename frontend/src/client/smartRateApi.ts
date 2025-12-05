import { apiPost } from "./client";
import type { SmartRateTestRequest, SmartRateTestResult } from "../types/smartRate";

/**
 * Kör Smart Räntetest (frontend → backend).
 *
 * Backend endpoint:
 * POST /api/smartrate/test
 */
export async function runSmartRateTest(payload: SmartRateTestRequest) {
    return apiPost<SmartRateTestResult, SmartRateTestRequest>(
        "/api/smartrate/test",
        payload
    );
}