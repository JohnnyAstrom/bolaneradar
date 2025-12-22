export interface BrandConfig {
    padding?: number;
    background?: string;
    maxHeight?: number;
}

export const brandConfig: Record<string, BrandConfig> = {
    swedbank: {
        padding: 6,
        background: "white",
        maxHeight: 56,
    },
    seb: {
        padding: 0,
        background: "white",
        maxHeight: 48,
    },
    nordea: {
        padding: 4,
        background: "white",
        maxHeight: 42,
    },
    handelsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 40,
    },
    lansforsakringarbank: {
        padding: 6,
        background: "white",
        maxHeight: 90,
    },
    sbab: {
        padding: 4,
        background: "white",
        maxHeight: 42,
    },
    skandiabanken: {
        padding: 8,
        background: "white",
        maxHeight: 38,
    },
    danskebank: {
        padding: 6,
        background: "white",
        maxHeight: 32,
    },
    icabanken: {
        padding: 6,
        background: "white",
        maxHeight: 36,
    },
    landshypotekbank: {
        padding: 4,
        background: "white",
        maxHeight: 85,
    },
    ikanobank: {
        padding: 8,
        background: "white",
        maxHeight: 56,
    },
    alandsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 32,
    },
};