import type { FC } from "react";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import BankIntroSection from "../components/bank/BankIntroSection";
import BankCurrentRatesTable from "../components/bank/BankCurrentRatesTable";
import BankGraphSection from "../components/bank/BankGraphSection";

import { bankDisplayNames } from "../config/bankDisplayNames";
import { bankNameMap } from "../config/bankNameMap";

import { getBankRates } from "../client/bankApi";
import type { BankRateResponse } from "../client/bankApi";

import { useBankIntro } from "../hooks/useBankIntro";
import BankDetailsSection from "../components/bank/BankDetailsSection.tsx";

/**
 * Karta över logotyper per bank.
 * Nyckeln (vänster) måste matcha URL-parametern.
 */
const logoMap: Record<string, string> = {
    swedbank: "/logos/swedbank.svg",
    seb: "/logos/seb.svg",
    nordea: "/logos/nordea.svg",
    handelsbanken: "/logos/handelsbanken.svg",
    lansforsakringarbank: "/logos/lansforsakringar.svg",
    sbab: "/logos/sbab.svg",
    skandiabanken: "/logos/skandiabanken.svg",
    danskebank: "/logos/danskebank.svg",
    icabanken: "/logos/icabanken.svg",
    landshypotekbank: "/logos/landshypotek.svg",
    ikanobank: "/logos/ikanobank.png",
    alandsbanken: "/logos/alandsbanken.svg",
};

const BankPage: FC = () => {
    const { bankName } = useParams();

    /**
     * URL-parametern (t.ex. "seb")
     * -> normaliseras till lowercase
     */
    const urlKey = bankName?.toLowerCase() ?? null;

    /**
     * I bankNameMap lagras API-nyckeln som backend förväntar sig.
     * Exempel:
     *   "lansforsakringarbank" -> "lansforsakringar"
     */
    const realBankName = urlKey ? bankNameMap[urlKey] : null;

    /* ============================================================
     * HÄMTA INTRODATA VIA HOOKEN
     * ============================================================ */
    const {
        data: introData,
        loading: introLoading,
        error: introError
    } = useBankIntro(urlKey || "");

    /* ============================================================
     * HÄMTA RÄNTETABELLEN
     * ============================================================ */
    const [rateData, setRateData] = useState<BankRateResponse | null>(null);
    const [ratesLoading, setRatesLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            if (!realBankName) {
                if (isMounted) setRatesLoading(false);
                return;
            }

            try {
                const res = await getBankRates(realBankName);
                if (isMounted) setRateData(res);
            } finally {
                if (isMounted) setRatesLoading(false);
            }
        };

        load();
        return () => { isMounted = false; };
    }, [realBankName]);

    /* ============================================================
     * FELHANTERING OCH LOADING
     * ============================================================ */
    if (!bankName) {
        return (
            <PageWrapper>
                <Section><p className="text-red-600">Kunde inte hitta banken.</p></Section>
            </PageWrapper>
        );
    }

    if (introLoading || ratesLoading) {
        return (
            <PageWrapper>
                <Section><p>Laddar...</p></Section>
            </PageWrapper>
        );
    }

    if (!rateData) {
        return (
            <PageWrapper>
                <Section><p className="text-red-600">Kunde inte läsa bankens räntedata.</p></Section>
            </PageWrapper>
        );
    }

    /* ============================================================
     * UTRÄKNA VISNINGSNAMN OCH LOGO
     * ============================================================ */
    const displayName = bankDisplayNames[urlKey!] ?? realBankName ?? urlKey!;
    const logoUrl = logoMap[urlKey!] ?? "";

    /* ============================================================
     * RENDERING
     * ============================================================ */
    return (
        <PageWrapper>

            {/* INTRO (nu dynamisk via API) */}
            <Section>
                {introData ? (
                    <BankIntroSection
                        bankKey={urlKey!}
                        logoUrl={logoUrl}
                        description={introData.description}
                        uspItems={introData.uspItems}
                    />
                ) : (
                    <p className="text-red-600">
                        {introError ?? `Kunde inte ladda introduktion för ${displayName}.`}
                    </p>
                )}
            </Section>

            {/* RÄNTOR + GRAF */}

            <Section>
                <div
                    className="
                    w-full max-w-full mx-auto
                    p-0 bg-transparent border-none rounded-none
                    sm:p-6 sm:bg-white sm:border sm:border-border sm:rounded-lg
                    sm:max-w-2xl
                    md:max-w-3xl
                    lg:max-w-4xl
                "
                >
                    {/* Aktuella räntor */}
                    <BankCurrentRatesTable
                        rows={rateData.rows}
                        averageMonthFormatted={rateData.monthFormatted}
                    />
                </div>
            </Section>
            <Section>
                <div
                    className="
                    w-full max-w-full mx-auto
                    p-0 bg-transparent border-none rounded-none
                    sm:p-6 sm:bg-white sm:border sm:border-border sm:rounded-lg
                    sm:max-w-2xl
                    md:max-w-3xl
                    lg:max-w-4xl
                "
                >
                    {/* Historisk graf */}
                    <div className="w-full">
                        <BankGraphSection bankName={bankName} />
                    </div>
                </div>
            </Section>

            {/* Bankdetails */}
            <Section>
                <BankDetailsSection bankKey={urlKey!} />
            </Section>

        </PageWrapper>
    );
};

export default BankPage;