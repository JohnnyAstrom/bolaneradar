export interface MortgageRate {
    id: number;
    bankName: string;
    term: string;
    rateType: string;
    ratePercent: number;
    effectiveDate: string;
}