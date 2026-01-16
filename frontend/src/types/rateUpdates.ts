export interface RateUpdateDay {
    date: string;
    updates: RateUpdate[];
}

export interface RateUpdate {
    bankKey: string;
    bankName: string;
    bindingPeriod: string;
    previousRate: number;
    newRate: number;
}