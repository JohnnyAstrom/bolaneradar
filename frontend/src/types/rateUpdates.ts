export interface RateUpdateDay {
    date: string;
    updates: RateUpdate[];
}

export interface RateUpdate {
    bankName: string;
    bindingPeriod: string;
    previousRate: number;
    newRate: number;
}