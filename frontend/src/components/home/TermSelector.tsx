import type { FC } from "react";
import { useTranslation } from "react-i18next";

interface TermSelectorProps {
    activeTerm: string;
    onSelectTerm: (term: string) => void;
}

const shortTerms = [
    { value: "3m" },
    { value: "1y" },
    { value: "2y" },
    { value: "3y" },
];

const longTerms = [
    { value: "4y" },
    { value: "5y" },
    { value: "7y" },
    { value: "10y" },
];

const TermSelector: FC<TermSelectorProps> = ({
                                                 activeTerm,
                                                 onSelectTerm,
                                             }) => {
    const { t } = useTranslation();

    const renderGroup = (
        titleKey: string,
        terms: { value: string }[]
    ) => (
        <div>
            <p className="text-sm font-semibold tracking-tight text-text-primary mb-2.5 px-1">
                {t(titleKey)}
            </p>

            <div className="flex gap-3 flex-wrap justify-start px-1">
                {terms.map((tTerm) => (
                    <button
                        key={tTerm.value}
                        onClick={() => onSelectTerm(tTerm.value)}
                        className={`
                            relative inline-flex items-center justify-center
                            px-4 py-2.5 rounded-2xl border min-w-[84px] text-center
                            text-sm font-medium transition-all duration-200
                            ${activeTerm === tTerm.value
                            ? "bg-primary text-white border-primary shadow-sm shadow-primary/20 -translate-y-[1px]"
                            : "bg-white text-text-secondary border-border hover:bg-slate-50 hover:border-slate-300 hover:-translate-y-[1px]"
                        }
                        `}
                    >
                        {t(`rates.termSelector.terms.${tTerm.value}`)}
                    </button>
                ))}
            </div>
        </div>
    );

    return (
        <div className="flex flex-col gap-7 mb-6">
            {renderGroup(
                "rates.termSelector.shortTitle",
                shortTerms
            )}
            {renderGroup(
                "rates.termSelector.longTitle",
                longTerms
            )}
        </div>
    );
};

export default TermSelector;
