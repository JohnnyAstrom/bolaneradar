import type { FC } from "react";
import { useParams } from "react-router-dom";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import BankIntroSection from "../components/bank/BankIntroSection";
import BankCurrentRatesTable from "../components/bank/BankCurrentRatesTable";
import BankGraphSection from "../components/bank/BankGraphSection";

import { bankDisplayNames } from "../config/bankDisplayNames";

// Map bankKey → logo path
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

    if (!bankName) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">Kunde inte hitta banken.</p>
                </Section>
            </PageWrapper>
        );
    }

    const bankKey = bankName;
    const displayName = bankDisplayNames[bankKey] ?? bankKey;
    const logoUrl = logoMap[bankKey];

    return (
        <PageWrapper>

            {/* --- Bank intro sektion (egen sektion, ingen card) --- */}
            <Section>
                <BankIntroSection
                    bankKey={bankKey}
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

            {/* --- Card med räntor + graf --- */}
            <Section>
                <div className="border border-border rounded-lg p-6 bg-white">

                    <BankCurrentRatesTable />

                    <div className="my-10 border-t border-border" />

                    <BankGraphSection />

                </div>
            </Section>

        </PageWrapper>
    );
};

export default BankPage;