import type { FC } from "react";
import { Check, X } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useBankDetails } from "../../hooks/useBankDetails";
import { bankDisplayNames } from "../../config/bankDisplayNames";
import { Link } from "react-router-dom";

interface BankDetailsSectionProps {
    bankKey: string;
}

const BankDetailsSection: FC<BankDetailsSectionProps> = ({ bankKey }) => {
    const { t } = useTranslation();
    const { details, loading, error } = useBankDetails(bankKey);

    const displayName = bankDisplayNames[bankKey] ?? bankKey;

    if (loading) {
        return <p>{t("common.loading")}</p>;
    }

    if (error || !details) {
        return (
            <p className="text-red-600">
                {t("bank.details.error")}
            </p>
        );
    }

    return (
        <div
            className="
                w-full max-w-5xl mx-auto px-1 sm:px-6
            "
        >
            <div className="p-1 sm:p-0 lg:rounded-[24px] lg:border lg:border-slate-200 lg:bg-white lg:p-6 lg:shadow-sm">
                {/* Titel */}
                <h2 className="text-2xl font-semibold text-text-primary mb-4">
                    {t("bank.details.about", { bank: displayName })}
                </h2>

                {/* Översiktstext */}
                <p className="max-w-3xl text-text-secondary mb-8 leading-8">
                    {details.overviewText}
                </p>

                <div className="mb-10 grid gap-5 lg:grid-cols-2">
                    <div className="rounded-[24px] border border-emerald-100 bg-emerald-50/50 p-5 sm:p-6">
                        <h3 className="text-lg font-semibold text-text-primary mb-4">
                            {t("bank.details.bestFor")}
                        </h3>

                        <ul className="space-y-3">
                            {details.bestFor.map((item: string, i: number) => (
                                <li key={i} className="flex items-start gap-3">
                                    <span className="mt-0.5 inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-white text-primary shadow-sm">
                                        <Check size={16} />
                                    </span>
                                    <span className="leading-7">{item}</span>
                                </li>
                            ))}
                        </ul>
                    </div>

                    <div className="rounded-[24px] border border-rose-100 bg-rose-50/50 p-5 sm:p-6">
                        <h3 className="text-lg font-semibold text-text-primary mb-4">
                            {t("bank.details.notFor")}
                        </h3>

                        <ul className="space-y-3">
                            {details.notFor.map((item: string, i: number) => (
                                <li key={i} className="flex items-start gap-3">
                                    <span className="mt-0.5 inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-white text-red-500 shadow-sm">
                                        <X size={16} />
                                    </span>
                                    <span className="leading-7">{item}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>

                {/* CTA Sektion */}
                <div className="pt-6 mt-6 border-t border-border flex flex-wrap items-center gap-3 sm:gap-5">

                    {/* Sekundär CTA — Läs mer om banken */}
                    {details.secondaryCtaLabel && (
                        <Link
                            to={`/bank/${bankKey}/info`}
                            className="
                                inline-flex items-center justify-center
                                px-5 py-3
                                border border-primary text-primary rounded-xl text-sm font-medium
                                whitespace-nowrap
                                hover:bg-primary/10 active:bg-primary/20
                                transition
                                flex-1 sm:flex-none
                            "
                        >
                            {details.secondaryCtaLabel}
                        </Link>
                    )}

                    {/* Primär CTA — besök bankens hemsida */}
                    {details.primaryCtaLabel && details.primaryCtaUrl && (
                        <a
                            href={details.primaryCtaUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="
                                inline-flex items-center justify-center
                                px-6 py-3
                                bg-primary text-white rounded-xl text-sm font-medium
                                whitespace-nowrap
                                hover:bg-primary-hover active:bg-primary-active
                                transition
                                flex-1 sm:flex-none
                            "
                        >
                            {details.primaryCtaLabel}
                        </a>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BankDetailsSection;
