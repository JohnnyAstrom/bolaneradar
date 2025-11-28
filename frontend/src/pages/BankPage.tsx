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

    const [data, setData] = useState<BankRateResponse | null>(null);
    const [loading, setLoading] = useState(true);

    const urlKey = bankName?.toLowerCase() ?? null;
    const realBankName = urlKey ? bankNameMap[urlKey] : null;

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            if (!realBankName) {
                if (isMounted) setLoading(false);
                return;
            }

            try {
                const res = await getBankRates(realBankName);
                if (isMounted) setData(res);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();
        return () => { isMounted = false; };
    }, [realBankName]);

    if (!bankName) {
        return (
            <PageWrapper>
                <Section><p className="text-red-600">Kunde inte hitta banken.</p></Section>
            </PageWrapper>
        );
    }

    if (loading) {
        return (
            <PageWrapper>
                <Section><p>Laddar...</p></Section>
            </PageWrapper>
        );
    }

    if (!data) {
        return (
            <PageWrapper>
                <Section><p className="text-red-600">Kunde inte läsa bankens räntedata.</p></Section>
            </PageWrapper>
        );
    }

    const displayName = bankDisplayNames[urlKey!] ?? realBankName ?? urlKey!;
    const logoUrl = logoMap[urlKey!];

    return (
        <PageWrapper>

            {/* INTRO */}
            <Section>
                <BankIntroSection
                    bankKey={urlKey!}
                    logoUrl={logoUrl}
                    description={`${displayName} är en etablerad svensk bank. Här kommer senare dynamisk fakta.`}
                    uspItems={["Placeholder USP 1", "Placeholder USP 2", "Placeholder USP 3"]}
                    primaryCtaLabel={`Gå till ${displayName}s bolånesida`}
                    secondaryCtaLabel={`Läs mer om ${displayName}`}
                />
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
                        rows={data.rows}
                        averageMonthFormatted={data.monthFormatted}
                    />

                    <div className="my-8 border-t border-border" />

                    {/* Graf — nu får den full mobilbredd */}
                    <div className="w-full">
                        <BankGraphSection bankName={bankName} />
                    </div>
                </div>

            </Section>
        </PageWrapper>
    );
};

export default BankPage;