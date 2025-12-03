export interface MortgageTermOption {
    value: string;
    label: string;
}

export const mortgageTermOptions: MortgageTermOption[] = [
    { value: "VARIABLE_3M", label: "Rörlig (3 månader)" },
    { value: "FIXED_1Y",    label: "1 år bunden" },
    { value: "FIXED_2Y",    label: "2 år bunden" },
    { value: "FIXED_3Y",    label: "3 år bunden" },
    { value: "FIXED_4Y",    label: "4 år bunden" },
    { value: "FIXED_5Y",    label: "5 år bunden" },
    { value: "FIXED_6Y",    label: "6 år bunden" },
    { value: "FIXED_7Y",    label: "7 år bunden" },
    { value: "FIXED_8Y",    label: "8 år bunden" },
    { value: "FIXED_9Y",    label: "9 år bunden" },
    { value: "FIXED_10Y",   label: "10 år bunden" },
];