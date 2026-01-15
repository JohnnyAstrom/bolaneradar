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
                w-full mx-auto
                bg-white rounded-md
                p-1 mt-0
                sm:rounded-lg sm:p-6
                max-w-2xl sm:max-w-4xl
            "
        >
            {/* Titel */}
            <h2 className="text-xl font-semibold text-text-primary mb-4">
                {t("bank.details.about", { bank: displayName })}
            </h2>

            {/* Översiktstext */}
            <p className="text-text-secondary mb-8 leading-relaxed">
                {details.overviewText}
            </p>

            {/* Passar bäst för */}
            <div className="mb-10">
                <h3 className="text-lg font-semibold text-text-primary mb-4">
                    {t("bank.details.bestFor")}
                </h3>

                <ul className="space-y-3">
                    {details.bestFor.map((item: string, i: number) => (
                        <li key={i} className="flex items-start gap-2">
                            <Check size={18} className="text-primary mt-1" />
                            <span>{item}</span>
                        </li>
                    ))}
                </ul>
            </div>

            {/* Mindre bra för */}
            <div className="mb-10">
                <h3 className="text-lg font-semibold text-text-primary mb-4">
                    {t("bank.details.notFor")}
                </h3>

                <ul className="space-y-3">
                    {details.notFor.map((item: string, i: number) => (
                        <li key={i} className="flex items-start gap-2">
                            <X size={18} className="text-red-500 mt-1" />
                            <span>{item}</span>
                        </li>
                    ))}
                </ul>
            </div>

            {/* CTA Sektion */}
            <div className="pt-6 mt-6 border-t border-border flex flex-wrap gap-3 sm:gap-5">

                {/* Sekundär CTA — Läs mer om banken */}
                {details.secondaryCtaLabel && (
                    <Link
                        to={`/bank/${bankKey}/info`}
                        className="
                            inline-flex items-center justify-center
                            px-5 py-3
                            border border-primary text-primary rounded-lg text-sm font-medium
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
                            px-5 py-3
                            bg-primary text-white rounded-lg text-sm font-medium
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
    );
};

export default BankDetailsSection;