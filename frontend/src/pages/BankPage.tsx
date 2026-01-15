import type {FC} from "react";
import {useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import BankIntroSection from "../components/bank/BankIntroSection";
import BankCurrentRatesTable from "../components/bank/BankCurrentRatesTable";
import BankGraphSection from "../components/bank/BankGraphSection";
import BankDetailsSection from "../components/bank/BankDetailsSection";
import BankLogo from "../components/bank/BankLogo";

import {bankDisplayNames} from "../config/bankDisplayNames";
import { bankLogos } from "../config/bankLogos";

import {preloadImage} from "../utils/preloadImage";

import {getBankRates} from "../services/bankApi";
import type {BankRateResponse} from "../services/bankApi";

import {useBankIntro} from "../hooks/useBankIntro";

const BankPage: FC = () => {
    const {bankKey} = useParams();
    const {t} = useTranslation();

    /* ============================================================
     * PRELOAD LOGO
     * ============================================================ */
    useEffect(() => {
        if (!bankKey) return;

        const logoUrl = bankLogos[bankKey];
        if (logoUrl) {
            preloadImage(logoUrl);
        }
    }, [bankKey]);

    /* ============================================================
     * INTRODATA
     * ============================================================ */
    const {
        data: introData,
        loading: introLoading,
        error: introError,
    } = useBankIntro(bankKey || "");

    /* ============================================================
     * RÄNTOR
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
                    <p className="text-red-600">{t("bank.errors.notFound")}</p>
                </Section>
            </PageWrapper>
        );
    }

    const displayName = bankDisplayNames[bankKey] ?? bankKey;
    const logoUrl = bankLogos[bankKey] ?? "";

    /* ============================================================
     * RENDERING
     * ============================================================ */
    return (
        <PageWrapper>

            {/* INTRO + LOGO */}
            <Section>
                {/* GEMENSAM LAYOUT-CONTAINER */}
                <div
                    className="
                    max-w-4xl mx-auto
                    pt-0 pb-0 px-0
                    sm:py-4 sm:px-6
                "
                >
                    <BankLogo
                        src={logoUrl}
                        alt={displayName}
                        bankKey={bankKey}
                    />

                    {introLoading ? (
                        <p>{t("common.loading")}</p>
                    ) : introData ? (
                        <BankIntroSection
                            description={introData.description}
                            uspItems={introData.uspItems}
                        />
                    ) : (
                        <p className="text-red-600">
                            {introError ??
                                t("bank.errors.intro", {bank: displayName})}
                        </p>
                    )}
                </div>
            </Section>

            {/* RÄNTOR */}
            <Section>
                {ratesLoading ? (
                    <p>{t("common.loading")}</p>
                ) : rateData ? (
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
                ) : (
                    <p className="text-red-600">
                        {t("bank.errors.rateData")}
                    </p>
                )}
            </Section>

            {/* HISTORISK GRAF */}
            <Section>
                <div
                    className="
                    max-w-4xl mx-auto
                    pt-0 pb-0 px-0
                    sm:py-4 sm:px-6
                "
                >
                    <BankGraphSection bankName={bankKey}/>
                </div>
            </Section>

            {/* BANK DETAILS */}
            <Section>
                <BankDetailsSection bankKey={bankKey}/>
            </Section>

        </PageWrapper>
    );
};

export default BankPage;