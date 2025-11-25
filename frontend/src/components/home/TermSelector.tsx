import type { FC } from "react";

interface TermSelectorProps {
    activeTerm: string;
    onSelectTerm: (term: string) => void;
}

const shortTerms = [
    { label: "3 mån", value: "3m" },
    { label: "1 år", value: "1y" },
    { label: "2 år", value: "2y" },
    { label: "3 år", value: "3y" },
];

const longTerms = [
    { label: "4 år", value: "4y" },
    { label: "5 år", value: "5y" },
    { label: "7 år", value: "7y" },
    { label: "10 år", value: "10y" },
];

const TermSelector: FC<TermSelectorProps> = ({
                                                 activeTerm,
                                                 onSelectTerm,
                                             }) => {
    const renderGroup = (
        title: string,
        terms: { label: string; value: string }[]
    ) => (
        <div>
            <p className="text-sm font-semibold text-text-primary mb-2">
                {title}
            </p>

            <div className="flex gap-3 flex-wrap">
                {terms.map((t) => (
                    <button
                        key={t.value}
                        onClick={() => onSelectTerm(t.value)}
                        className={`
              px-4 py-2 rounded-lg border transition-colors
              ${
                            activeTerm === t.value
                                ? "bg-primary text-white border-primary"
                                : "bg-white text-text-secondary border-border hover:bg-row-hover"
                        }
            `}
                    >
                        {t.label}
                    </button>
                ))}
            </div>
        </div>
    );

    return (
        <div className="flex flex-col gap-6 mb-6">
            {renderGroup("Korta bindningstider (3 mån - 3 år)", shortTerms)}
            {renderGroup("Längre bindningstider (4 år - 10 år)", longTerms)}
        </div>
    );
};

export default TermSelector;