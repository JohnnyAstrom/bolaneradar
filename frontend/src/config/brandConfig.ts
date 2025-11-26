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
        maxHeight: 56,
    },
    nordea: {
        padding: 4,
        background: "white",
        maxHeight: 56,
    },
    handelsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 56,
    },
    lansforsakringarbank: {
        padding: 6,
        background: "white",
        maxHeight: 56,
    },
    sbab: {
        padding: 4,
        background: "white",
        maxHeight: 56,
    },
    skandiabanken: {
        padding: 8,
        background: "white",
        maxHeight: 56,
    },
    danskebank: {
        padding: 6,
        background: "white",
        maxHeight: 56,
    },
    icabanken: {
        padding: 6,
        background: "white",
        maxHeight: 56,
    },
    landshypotekbank: {
        padding: 4,
        background: "white",
        maxHeight: 56,
    },
    ikanobank: {
        padding: 8,
        background: "white",
        maxHeight: 56,
    },
    alandsbanken: {
        padding: 4,
        background: "white",
        maxHeight: 56,
    },
};