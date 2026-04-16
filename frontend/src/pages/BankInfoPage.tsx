import { useParams, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useBankInfo } from "../hooks/useBankInfo";
import { ChevronDown } from "lucide-react";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";
import BankLogo from "../components/bank/BankLogo";

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
            <Section contentClassName="rounded-[28px]">
                <div
                    className="
                        max-w-5xl mx-auto
                        px-1 sm:px-6
                    "
                >
                    <div className="p-1 sm:p-0 lg:rounded-[24px] lg:border lg:border-slate-200 lg:bg-white lg:p-6 lg:shadow-sm">
                        <div className="mb-4">
                            {logoUrl && (
                                <BankLogo
                                    src={logoUrl}
                                    alt={displayName}
                                    bankKey={bankKey!}
                                />
                            )}
                        </div>

                        <div>
                            <p className="max-w-4xl text-base leading-8 text-text-secondary sm:text-lg">
                                {info.intro}
                            </p>
                        </div>
                    </div>
                </div>
            </Section>

            {/* FÖRDJUPAD ANALYS */}
            <Section contentClassName="rounded-[28px]">
                <div
                    className="
                        max-w-5xl mx-auto
                        px-1 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.deepAnalysis")}
                    </h2>

                    <div className="grid gap-4">
                        {info.deepInsights.map((point, i) => (
                            <div
                                key={i}
                                className="rounded-[22px] border border-slate-200 bg-white p-5 shadow-sm"
                            >
                                <h3 className="text-lg font-semibold text-text-primary mb-2">
                                    {point.heading}
                                </h3>
                                <p className="max-w-4xl text-text-secondary leading-8">
                                    {point.text}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </Section>

            {/* FAQ */}
            <Section contentClassName="rounded-[28px]">
                <div
                    className="
                        max-w-5xl mx-auto
                        px-1 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-4 text-text-primary">
                        {t("bank.info.faq")}
                    </h2>

                    <div className="grid gap-3">
                        {info.faq.map((f, i) => (
                            <details
                                key={i}
                                className="group rounded-[20px] border border-slate-200 bg-white p-4 shadow-sm"
                            >
                                <summary className="flex cursor-pointer list-none items-center justify-between gap-4 pr-1 text-base font-semibold text-text-primary marker:content-none">
                                    <span>{f.question}</span>
                                    <ChevronDown
                                        size={18}
                                        className="shrink-0 text-slate-400 transition-transform duration-200 group-open:rotate-180"
                                    />
                                </summary>
                                <p className="mt-3 leading-8 text-text-secondary">
                                    {f.answer}
                                </p>
                            </details>
                        ))}
                    </div>

                    <div className="mt-6 border-t border-slate-200 pt-6">
                        <div className="flex flex-col sm:flex-row gap-4">
                            <Link
                                to={`/bank/${bankKey}`}
                                className="
                                    inline-flex items-center justify-center
                                    px-5 py-3
                                    border border-primary text-primary rounded-xl
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
                                    px-6 py-3
                                    bg-primary text-white rounded-xl
                                    text-sm font-medium
                                    hover:bg-primary-hover active:bg-primary-active
                                    transition
                                "
                            >
                                {info.ctaLabel}
                            </a>
                        </div>
                    </div>
                </div>
            </Section>

        </PageWrapper>
    );
}
