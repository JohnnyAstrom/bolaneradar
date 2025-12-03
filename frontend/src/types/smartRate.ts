/**
 * Payload för Smart Räntetest.
 *
 * Användaren fyller i detta formulär och datan skickas till backend.
 */
export interface SmartRateTestRequest {
    bank: string;
    hasOffer: boolean;

    // Flöde A – ingen offert
    currentRate?: number;
    currentRateTerm?: string;
    rateChangeDate?: string;
    bindingEndDate?: string;
    futureRatePreference?: string;

    // Flöde B – har offert
    offerBindingTerm?: string;
    offerRate?: number;
    offerStartDate?: string;
    offerComparisonTarget?: string;
}

/**
 * Resultat från Smart Räntetest.
 *
 * Backend analyserar input och returnerar ett strukturerat resultat.
 */
export interface SmartRateTestResult {
    status: string;
    bank: string;
    analyzedTerm: string;
    differenceFromBankAverage: number;
    differenceFromBestMarketAverage: number;
    analysisText: string;
    additionalContext: string;
    recommendation: string;
}