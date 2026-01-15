import { useParams, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useBankInfo } from "../hooks/useBankInfo";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import { bankDisplayNames } from "../config/bankDisplayNames";
import { bankLogos } from "../config/bankLogos";

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
    const logoUrl = bankKey ? bankLogos[bankKey] ?? null : null;

    return (
        <PageWrapper>

            {/* HERO / INTRO */}
            <Section>
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
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
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.deepAnalysis")}
                    </h2>

                    <div className="space-y-4">
                        {info.deepInsights.map((point, i) => (
                            <div
                                key={i}
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
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.faq")}
                    </h2>

                    <div className="space-y-4">
                        {info.faq.map((f, i) => (
                            <div
                                key={i}
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
                        w-full max-w-full mx-auto
                        p-2 bg-transparent border-none rounded-none
                        sm:p-6 sm:bg-white sm:border sm:border-border sm:rounded-lg
                        sm:max-w-2xl md:max-w-3xl lg:max-w-4xl
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