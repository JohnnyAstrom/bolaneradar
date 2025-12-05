/**
 * Payload för Smart Räntetest (frontend → backend)
 *
 * Detta är exakt vad backend Version 4 förväntar sig.
 */
export interface SmartRateTestRequest {
    // Bank
    bankName: string;
    bankId: number;
    hasOffer: boolean;

    // Flöde A (ingen offert)
    userRate?: number;
    userCurrentTerm?: string;      // MortgageTerm enum (som string)
    bindingEndDate?: string;       // YYYY-MM-DD
    userPreference?: string;       // RatePreference enum (string)

    // Flöde B (har offert)
    offerTerm?: string;            // MortgageTerm enum (string)
    offerRate?: number;

    // Gemensamt
    loanAmount?: number;           // Valfritt i backend, men recommended
}

/**
 * Resultat från Smart Räntetest (backend → frontend)
 *
 * Detta motsvarar exakt SmartRateTestResult i backend Version 4.
 */
export interface SmartRateTestResult {
    status: string;                     // GREEN / YELLOW / RED / INFO
    bank: string;
    analyzedTerm: string;

    differenceFromBankAverage: number | null;
    differenceFromBestMarketAverage: number | null;

    analysisText: string;
    additionalContext: string;
    recommendation: string;

    // Version 3 — sparpotential
    yearlySaving?: number | null;

    // Version 3 — preferensbaserat råd
    preferenceAdvice?: string | null;

    // Version 4 — Alternativlista baserat på preferenser
    alternatives?: {
        term: string;                     // MortgageTerm enum
        averageRate: number;              // bankens snitt
        differenceFromBest: number;       // diff mot bästa marknaden
        yearlyCostDifference: number | null;
    }[];

    alternativesIntro?: string;

    isOfferFlow?: boolean;
}