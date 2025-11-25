import type { FC } from "react";
import { useParams } from "react-router-dom";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

// Map bankName → logo path
const logoMap: Record<string, string> = {
    swedbank: "/logos/swedbank.svg",
    seb: "/logos/seb.svg",
    nordea: "/logos/nordea.svg",
    handelsbanken: "/logos/handelsbanken.svg",
    länsförsäkringarbank: "/logos/lansforsakringar.svg",
    sbab: "/logos/sbab.svg",
    skandiabanken: "/logos/skandiabanken.png",
    danskebank: "/logos/danskebank.svg",
    icabanken: "/logos/icabanken.svg",
    landshypotekbank: "/logos/landshypotek.svg",
    ikanobank: "/logos/ikanobank.png",
    ålandsbanken: "/logos/alandsbanken.png",
};

const BankPage: FC = () => {
    const { bankName } = useParams();

    const key = bankName ? bankName.toLowerCase().replace(/\s+/g, "") : "";
    const logoUrl = logoMap[key];

    const displayName =
        bankName ? bankName.charAt(0).toUpperCase() + bankName.slice(1) : "";

    return (
        <PageWrapper>

            <Section>

                {/* Bank Header */}
                <div className="mb-6 flex items-center gap-4">

                    {/* Logo */}
                    {logoUrl && (
                        <img
                            src={logoUrl}
                            alt={`${displayName} logotyp`}
                            className="w-16 h-16 object-contain rounded"
                        />
                    )}

                    <div>
                        <h1 className="text-3xl font-bold text-text-primary mb-1">
                            {displayName}
                        </h1>

                        <p className="text-text-secondary text-sm max-w-2xl">
                            Här visas information om räntor, utveckling, förändringar och
                            snitträntor för {displayName}. Denna sida kommer senare inkludera:
                            historik, trender, grafer och alla bindningstider.
                        </p>
                    </div>

                </div>

                {/* Placeholder content block */}
                <div className="border border-border rounded-lg p-6 bg-white text-text-secondary">
                    <p>Innehåll kommer snart: historikdiagram, snitträntor och detaljerad data.</p>
                </div>

            </Section>

        </PageWrapper>
    );
};

export default BankPage;