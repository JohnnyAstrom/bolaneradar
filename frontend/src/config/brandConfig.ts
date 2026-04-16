export interface BrandConfig {
    padding?: number;
    background?: string;
    maxHeight?: number;
    tableMaxHeight?: number;
    tableMaxWidth?: number;
    tableScale?: number;
}

export const brandConfig: Record<string, BrandConfig> = {
    swedbank: {
        padding: 6,
        background: "white",
        maxHeight: 56,
        tableMaxHeight: 24,
        tableMaxWidth: 112,
        tableScale: 1,
    },
    seb: {
        padding: 0,
        background: "white",
        maxHeight: 48,
        tableMaxHeight: 17,
        tableMaxWidth: 88,
        tableScale: 0.86,
    },
    nordea: {
        padding: 4,
        background: "white",
        maxHeight: 42,
        tableMaxHeight: 18,
        tableMaxWidth: 96,
        tableScale: 0.82,
    },
    handelsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 40,
        tableMaxHeight: 22,
        tableMaxWidth: 112,
        tableScale: 1,
    },
    lansforsakringarbank: {
        padding: 6,
        background: "white",
        maxHeight: 90,
        tableMaxHeight: 21,
        tableMaxWidth: 92,
        tableScale: 1.45,
    },
    sbab: {
        padding: 4,
        background: "white",
        maxHeight: 42,
        tableMaxHeight: 18,
        tableMaxWidth: 92,
        tableScale: 0.82,
    },
    skandiabanken: {
        padding: 8,
        background: "white",
        maxHeight: 38,
        tableMaxHeight: 15,
        tableMaxWidth: 88,
        tableScale: 0.86,
    },
    danskebank: {
        padding: 6,
        background: "white",
        maxHeight: 32,
        tableMaxHeight: 18,
        tableMaxWidth: 96,
        tableScale: 1,
    },
    icabanken: {
        padding: 6,
        background: "white",
        maxHeight: 36,
        tableMaxHeight: 18,
        tableMaxWidth: 92,
        tableScale: 0.88,
    },
    landshypotekbank: {
        padding: 4,
        background: "white",
        maxHeight: 85,
        tableMaxHeight: 22,
        tableMaxWidth: 84,
        tableScale: 1.65,
    },
    ikanobank: {
        padding: 8,
        background: "white",
        maxHeight: 56,
        tableMaxHeight: 18,
        tableMaxWidth: 76,
        tableScale: 1.28,
    },
    alandsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 32,
        tableMaxHeight: 18,
        tableMaxWidth: 100,
        tableScale: 1.3,
    },
};
