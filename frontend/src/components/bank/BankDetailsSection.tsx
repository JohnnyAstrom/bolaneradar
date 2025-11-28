import type { FC } from "react";
import { Check, X } from "lucide-react";
import { useBankDetails } from "../../hooks/useBankDetails";
import { bankDisplayNames } from "../../config/bankDisplayNames";

interface BankDetailsSectionProps {
    bankKey: string;
}

const BankDetailsSection: FC<BankDetailsSectionProps> = ({ bankKey }) => {
    const { details, loading, error } = useBankDetails(bankKey);

    const displayName = bankDisplayNames[bankKey] ?? bankKey;

    if (loading) return <p>Laddar bankinformation…</p>;
    if (error || !details) return <p className="text-red-600">Kunde inte läsa bankinformationen.</p>;

    return (
        <div className="
            w-full mx-auto
            bg-white rounded-md
            p-0 mt-0
            sm:rounded-lg sm:p-6
            max-w-2xl sm:max-w-4xl
        ">
            <h2 className="text-xl font-semibold text-text-primary mb-4">
                Om {displayName}
            </h2>

            {/* Översiktstext */}
            <p className="text-text-secondary mb-8 leading-relaxed">
                {details.overviewText}
            </p>

            {/* Best for */}
            <div className="mb-10">
                <h3 className="text-lg font-semibold text-text-primary mb-4">
                    Passar bäst för
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

            {/* Not for */}
            <div className="mb-10">
                <h3 className="text-lg font-semibold text-text-primary mb-4">
                    Mindre bra för
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

            {/* CTA-knappar */}
            <div className="flex flex-wrap gap-3 sm:gap-5 mt-6">
                {details.primaryCtaLabel && details.primaryCtaUrl && (
                    <a
                        href={details.primaryCtaUrl}
                        className="
                          inline-flex items-center justify-center
                          px-5 py-3
                          max-w-[48%]
                          bg-primary text-white rounded-lg text-sm font-medium
                          whitespace-nowrap
                          hover:bg-primary-hover active:bg-primary-active transition
                      "
                    >
                        {details.primaryCtaLabel}
                    </a>

                )}

                {details.secondaryCtaLabel && details.secondaryCtaUrl && (
                    <a
                        href={details.secondaryCtaUrl}
                        className="
                          inline-flex items-center justify-center
                          px-5 py-3
                          max-w-[48%]
                          border border-primary text-primary rounded-lg text-sm font-medium
                          whitespace-nowrap
                          hover:bg-primary/10 active:bg-primary/20 transition
                      "
                    >
                        {details.secondaryCtaLabel}
                    </a>

                )}
            </div>
        </div>
    );
};

export default BankDetailsSection;