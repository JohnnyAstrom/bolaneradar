/**
 * Payload för Smart Räntetest (frontend → backend)
 *
 * Detta är exakt vad backend Version 5 förväntar sig (multi-offer).
 */

// Ett erbjudande från kunden
export interface SmartRateOfferDto {
    term: string;     // MortgageTerm enum (string)
    rate: number;     // Räntesatsen kunden angett
}

export interface SmartRateTestRequest {
    // Bankinfo
    bankName: string;
    bankId: number;
    hasOffer: boolean;

    // Flöde A — ingen offert (kundens nuvarande ränta)
    userRate?: number;
    userCurrentTerm?: string;     // MortgageTerm som string
    bindingEndDate?: string;      // YYYY-MM-DD
    userPreference?: string;      // RatePreference enum

    // Flöde B — multipla erbjudanden
    offers?: SmartRateOfferDto[];

    // Gemensamt
    loanAmount?: number;
}

/**
 * Resultat från Smart Räntetest (backend → frontend)
 *
 * Motsvarar SmartRateTestResult i backend Version 5 (multi-offer).
 */

// Enskild analys av ett erbjudande
export interface SmartRateOfferAnalysisResultDto {
    term: string;
    offeredRate: number;
    diffFromBestMarket: number | null;
    diffFromMedianMarket: number | null;
    diffFromBankAverage: number | null;
    status: string;
    analysisText: string;
    recommendation: string;
    yearlyCostDifference: number | null;
}

export interface SmartRateTestResult {
    status: string;
    bank: string;
    analyzedTerm: string;

    differenceFromBankAverage: number | null;
    differenceFromBestMarketAverage: number | null;

    analysisText: string;
    additionalContext: string;
    recommendation: string;

    yearlySaving?: number | null;
    preferenceAdvice?: string | null;

    alternatives?: {
        term: string;
        averageRate: number;
        differenceFromBest: number;
        yearlyCostDifference: number | null;
    }[];

    alternativesIntro?: string;

    isOfferFlow?: boolean;

    // Analys av multipla erbjudanden (en rad per bindningstid)
    offerAnalyses?: SmartRateOfferAnalysisResultDto[];

    // Viktigt för frontend: avgör om vi visar enkel eller multi-offer layout
    multipleOffers?: boolean;
}