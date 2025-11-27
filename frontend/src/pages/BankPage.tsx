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

    // -----------------------
    // Hooks måste ligga här ↑
    // -----------------------

    const [data, setData] = useState<BankRateResponse | null>(null);
    const [loading, setLoading] = useState(true);

    // Får vi ingen bankName → vi avbryter senare, men hookarna körs först
    const urlKey = bankName?.toLowerCase() ?? null;
    const realBankName = urlKey ? bankNameMap[urlKey] : null;

    useEffect(() => {
        let isMounted = true; // skydd mot state-uppdatering efter unmount

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

        return () => {
            isMounted = false;
        };
    }, [realBankName]);

    // -----------------------
    // Tidiga returns efter hooks
    // -----------------------

    if (!bankName) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">Kunde inte hitta banken.</p>
                </Section>
            </PageWrapper>
        );
    }

    if (loading) {
        return (
            <PageWrapper>
                <Section>
                    <p>Laddar...</p>
                </Section>
            </PageWrapper>
        );
    }

    if (!data) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">Kunde inte läsa bankens räntedata.</p>
                </Section>
            </PageWrapper>
        );
    }

    // Övriga UI-variabler
    const displayName = bankDisplayNames[urlKey!] ?? realBankName ?? urlKey!;
    const logoUrl = logoMap[urlKey!];

    return (
        <PageWrapper>
            <Section>
                <BankIntroSection
                    bankKey={urlKey!}
                    logoUrl={logoUrl}
                    description={`${displayName} är en etablerad svensk bank. Här kommer senare dynamisk fakta.`}
                    uspItems={[
                        "Placeholder USP 1",
                        "Placeholder USP 2",
                        "Placeholder USP 3",
                    ]}
                    primaryCtaLabel={`Gå till ${displayName}s bolånesida`}
                    secondaryCtaLabel={`Läs mer om ${displayName}`}
                />
            </Section>

            <Section>
                <div className="border border-border rounded-lg p-6 bg-white">

                    <BankCurrentRatesTable
                        rows={data.rows}
                        averageMonthFormatted={data.monthFormatted}
                    />

                    <div className="my-10 border-t border-border" />

                    <BankGraphSection />

                </div>
            </Section>
        </PageWrapper>
    );
};

export default BankPage;