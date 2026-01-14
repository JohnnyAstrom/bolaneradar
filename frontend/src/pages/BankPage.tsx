import type { FC } from "react";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import BankIntroSection from "../components/bank/BankIntroSection";
import BankCurrentRatesTable from "../components/bank/BankCurrentRatesTable";
import BankGraphSection from "../components/bank/BankGraphSection";
import BankDetailsSection from "../components/bank/BankDetailsSection";
import BankLogo from "../components/bank/BankLogo";

import { bankDisplayNames } from "../config/bankDisplayNames";

import { getBankRates } from "../client/bankApi";
import type { BankRateResponse } from "../client/bankApi";

import { useBankIntro } from "../hooks/useBankIntro";

/**
 * Karta över logotyper per bank.
 * Nyckeln måste matcha URL-parametern.
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

/**
 * Preload av bilder (används för bankloggor)
 */
function preloadImage(src: string) {
    if (!src) return;
    const img = new Image();
    img.src = src;
}

const BankPage: FC = () => {
    const { bankKey } = useParams();
    const { t } = useTranslation();

    /* ============================================================
     * PRELOAD LOGO SÅ TIDIGT SOM MÖJLIGT
     * ============================================================ */
    useEffect(() => {
        if (!bankKey) return;

        const logoUrl = logoMap[bankKey];
        if (logoUrl) {
            preloadImage(logoUrl);
        }
    }, [bankKey]);

    /* ============================================================
     * HÄMTA INTRODATA
     * ============================================================ */
    const {
        data: introData,
        loading: introLoading,
        error: introError
    } = useBankIntro(bankKey || "");

    /* ============================================================
     * HÄMTA RÄNTOR
     * ============================================================ */
    const [rateData, setRateData] = useState<BankRateResponse | null>(null);
    const [ratesLoading, setRatesLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            if (!bankKey) {
                if (isMounted) setRatesLoading(false);
                return;
            }

            try {
                const res = await getBankRates(bankKey);
                if (isMounted) setRateData(res);
            } finally {
                if (isMounted) setRatesLoading(false);
            }
        };

        load();
        return () => {
            isMounted = false;
        };
    }, [bankKey]);

    /* ============================================================
     * FELHANTERING
     * ============================================================ */
    if (!bankKey) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">
                        {t("bank.errors.notFound")}
                    </p>
                </Section>
            </PageWrapper>
        );
    }

    if (introLoading || ratesLoading) {
        return (
            <PageWrapper>
                <Section>
                    <p>{t("common.loading")}</p>
                </Section>
            </PageWrapper>
        );
    }

    if (!rateData) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">
                        {t("bank.errors.rateData")}
                    </p>
                </Section>
            </PageWrapper>
        );
    }

    /* ============================================================
     * VISNINGSNAMN + LOGO
     * ============================================================ */
    const displayName =
        bankDisplayNames[bankKey] ??
        bankKey;

    const logoUrl = logoMap[bankKey] ?? "";

    /* ============================================================
     * RENDERING
     * ============================================================ */
    return (
        <PageWrapper>

            {/* LOGO – renderas direkt */}
            <Section>
                <BankLogo
                    src={logoUrl}
                    alt={displayName}
                    bankKey={bankKey}
                />
            </Section>

            {/* INTRO */}
            <Section>
                {introData ? (
                    <BankIntroSection
                        description={introData.description}
                        uspItems={introData.uspItems}
                    />
                ) : (
                    <p className="text-red-600">
                        {introError ?? t("bank.errors.intro", { bank: displayName })}
                    </p>
                )}
            </Section>

            {/* RÄNTOR */}
            <Section>
                <div
                    className="
                        w-full max-w-full mx-auto
                        p-0 bg-transparent border-none rounded-none
                        sm:p-6 sm:bg-white sm:border sm:border-border sm:rounded-lg
                        sm:max-w-2xl md:max-w-3xl lg:max-w-4xl
                    "
                >
                    <BankCurrentRatesTable
                        rows={rateData.rows}
                        averageMonthFormatted={rateData.monthFormatted}
                    />
                </div>
            </Section>

            {/* HISTORISK GRAF */}
            <Section>
                <div
                    className="
                        w-full max-w-full mx-auto
                        p-0 bg-transparent border-none rounded-none
                        sm:p-6 sm:bg-white sm:border sm:border-border sm:rounded-lg
                        sm:max-w-2xl md:max-w-3xl lg:max-w-4xl
                    "
                >
                    <BankGraphSection bankName={bankKey} />
                </div>
            </Section>

            {/* BANK DETAILS */}
            <Section>
                <BankDetailsSection bankKey={bankKey} />
            </Section>

        </PageWrapper>
    );
};

export default BankPage;