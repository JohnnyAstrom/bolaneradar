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
            <p className="text-sm font-semibold text-text-primary mb-2">
                {t(titleKey)}
            </p>

            <div className="flex gap-3 flex-wrap justify-start">
                {terms.map((tTerm) => (
                    <button
                        key={tTerm.value}
                        onClick={() => onSelectTerm(tTerm.value)}
                        className={`
                            px-4 py-2 rounded-lg border transition-colors min-w-[80px] text-center
                            ${activeTerm === tTerm.value
                            ? "bg-primary text-white border-primary"
                            : "bg-white text-text-secondary border-border hover:bg-row-hover"
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
        <div className="flex flex-col gap-6 mb-6">
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