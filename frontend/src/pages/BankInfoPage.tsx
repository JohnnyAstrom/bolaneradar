import { useParams, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useBankInfo } from "../hooks/useBankInfo";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import { bankDisplayNames } from "../config/bankDisplayNames";

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

export default function BankInfoPage() {
    const { bankKey } = useParams();
    const { t } = useTranslation();
    const { info, loading, error } = useBankInfo(bankKey);

    if (loading) {
        return (
            <PageWrapper>
                <Section>
                    <p>{t("common.loading")}</p>
                </Section>
            </PageWrapper>
        );
    }

    if (error || !info) {
        return (
            <PageWrapper>
                <Section>
                    <p className="text-red-600">
                        {t("bank.info.notFound")}
                    </p>
                </Section>
            </PageWrapper>
        );
    }

    const displayName = bankDisplayNames[bankKey!] ?? bankKey!;
    const logoUrl = logoMap[bankKey!] ?? null;

    return (
        <PageWrapper>

            {/* HERO / INTRO */}
            <Section>
                <div
                    className="
                        w-full mx-auto
                        bg-white rounded-md
                        p-0 mt-0
                        sm:rounded-lg sm:p-6
                        max-w-2xl sm:max-w-3xl
                    "
                >
                    <div className="mb-6">
                        {logoUrl && (
                            <img
                                src={logoUrl}
                                alt={displayName}
                                className="h-14 mb-4"
                            />
                        )}
                        <p className="text-text-secondary text-lg leading-relaxed">
                            {info.intro}
                        </p>
                    </div>
                </div>
            </Section>

            {/* FÖRDJUPAD ANALYS */}
            <Section>
                <div
                    className="
                        w-full mx-auto
                        bg-white rounded-md
                        p-0 mt-0
                        sm:rounded-lg sm:p-6
                        max-w-2xl sm:max-w-3xl
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.deepAnalysis")}
                    </h2>

                    <div className="space-y-4">
                        {info.deepInsights.map((point, i) => (
                            <div
                                key={i}
                                className="bg-white border border-border rounded-lg p-5"
                            >
                                <h3 className="font-medium text-text-primary mb-1">
                                    {point.heading}
                                </h3>
                                <p className="text-text-secondary leading-relaxed">
                                    {point.text}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </Section>

            {/* FAQ */}
            <Section>
                <div
                    className="
                        w-full mx-auto
                        bg-white rounded-md
                        p-0 mt-0
                        sm:rounded-lg sm:p-6
                        max-w-2xl sm:max-w-3xl
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.faq")}
                    </h2>

                    <div className="space-y-4">
                        {info.faq.map((f, i) => (
                            <div
                                key={i}
                                className="bg-white border border-border rounded-lg p-5"
                            >
                                <p className="font-medium text-text-primary mb-1">
                                    {f.question}
                                </p>
                                <p className="text-text-secondary leading-relaxed">
                                    {f.answer}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </Section>

            {/* CTA + Tillbaka */}
            <Section>
                <div
                    className="
                        w-full mx-auto
                        bg-white rounded-md
                        p-0 mt-0
                        sm:rounded-lg sm:p-6
                        max-w-2xl sm:max-w-3xl
                        text-center
                    "
                >
                    <div className="flex flex-col sm:flex-row gap-4 mt-4">

                        <Link
                            to={`/bank/${bankKey}`}
                            className="
                                inline-flex items-center justify-center
                                px-5 py-3
                                border border-primary text-primary rounded-lg
                                text-sm font-medium
                                hover:bg-primary/10 active:bg-primary/20
                                transition
                            "
                        >
                            {t("bank.info.backToBank", { bank: displayName })}
                        </Link>

                        <a
                            href={info.ctaUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="
                                inline-flex items-center justify-center
                                px-5 py-3
                                bg-primary text-white rounded-lg
                                text-sm font-medium
                                hover:bg-primary-hover active:bg-primary-active
                                transition
                            "
                        >
                            {info.ctaLabel}
                        </a>
                    </div>
                </div>
            </Section>

        </PageWrapper>
    );
}