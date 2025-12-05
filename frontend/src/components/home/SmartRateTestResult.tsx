import type { FC } from "react";
import type { SmartRateTestResult } from "../../types/smartRate";

interface Props {
    result: SmartRateTestResult | null | undefined;
}

const statusColors: Record<string, string> = {
    GREAT_GREEN: "bg-green-600",  // extra bra
    GREEN: "bg-green-500",
    YELLOW: "bg-yellow-500",
    ORANGE: "bg-orange-500",
    RED: "bg-red-500",

    INFO: "bg-blue-500",
    UNKNOWN: "bg-gray-500"
};

const statusLabels: Record<string, string> = {
    GREAT_GREEN: "Ovanligt bra ränta!",
    GREEN: "Bra ränta!",
    YELLOW: "Lite hög ränta!",
    ORANGE: "Hög ränta!",
    RED: "Mycket hög ränta!",

    INFO: "Viktig information!",
    UNKNOWN: "Okänt läge"
};

/** -----------------------------------
 *  Helper: Konsumentvänliga termer
 * ----------------------------------*/
function formatTerm(term: string): string {
    const map: Record<string, string> = {
        VARIABLE_3M: "Rörlig ränta (3 månader)",
        FIXED_1Y: "1 års bundet",
        FIXED_2Y: "2 års bundet",
        FIXED_3Y: "3 års bundet",
        FIXED_4Y: "4 års bundet",
        FIXED_5Y: "5 års bundet",
        FIXED_7Y: "7 års bundet",
        FIXED_8Y: "8 års bundet",
        FIXED_10Y: "10 års bundet"
    };

    return map[term] ?? term;
}

/** -----------------------------------
 *  Helper: procentformat max 2 decimals
 * ----------------------------------*/
function formatPercent(value: number | null): string {
    if (value === null || value === undefined) return "—";

    const formatted = value.toFixed(2).replace(/\.00$/, "");
    return `${formatted} %`;
}

/** -----------------------------------
 *  Helper: årskostnad/årssparing
 * ----------------------------------*/
function formatYearlyEffect(value: number | null): string {
    if (value === null || value === undefined) return "—";

    const rounded = Math.round(value);

    if (rounded === 0) return "0 kr / år";

    const abs = Math.abs(rounded).toLocaleString("sv-SE");

    return rounded > 0
        ? `${abs} kr dyrare / år`
        : `${abs} kr billigare / år`;
}

const SmartRateTestResultView: FC<Props> = ({ result }) => {

    if (!result) return null;

    const color = statusColors[result.status] || "bg-gray-500";

    // Är detta offer-flow?
    const isOfferFlow = result.alternativesIntro !== null && result.alternativesIntro !== undefined;

    return (
        <div className="flex flex-col gap-8 p-4 border border-border rounded-lg bg-white">

            {/* =============================== */}
            {/*  STATUS */}
            {/* =============================== */}
            <div>
                <h2 className="text-2xl font-bold text-text-primary mb-2">
                    {result.isOfferFlow
                        ? "Analys av ditt ränteerbjudande"
                        : "Din räntestatus"
                    }
                </h2>
                <span
                    className={`inline-block px-4 py-1 rounded-full text-white text-sm font-medium ${color}`}
                >
                    {statusLabels[result.status] ?? "Okänt läge"}
                </span>
            </div>

            {/* =============================== */}
            {/*  ANALYS */}
            {/* =============================== */}
            <div className="text-text-secondary leading-relaxed space-y-3">

                <p>{result.analysisText}</p>

                {result.additionalContext && (
                    <p>{result.additionalContext}</p>
                )}

                {result.recommendation && (
                    <p className="font-semibold text-primary mt-3">
                        {result.recommendation}
                    </p>
                )}
            </div>

            {/* =============================== */}
            {/*  PREFERENS-RÅD */}
            {/* =============================== */}
            {result.preferenceAdvice && (
                <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <h3 className="font-semibold mb-1">
                        Rådgivning baserat på dina preferenser
                    </h3>
                    <p>{result.preferenceAdvice}</p>
                </div>
            )}

            {/* =============================== */}
            {/*  ALTERNATIVLISTA */}
            {/* =============================== */}
            {result.alternatives && result.alternatives.length > 0 && (
                <div className="p-4 bg-gray-50 border border-border rounded-lg">

                    {/* Rubrik logik */}
                    <h3 className="font-semibold mb-3">

                        {isOfferFlow
                            ? "Hur ditt erbjudande står sig mot marknaden"
                            : "Räntor som passar din valda bindningstid"}

                    </h3>

                    {/* Intro från backend i offer-flow */}
                    {isOfferFlow && result.alternativesIntro && (
                        <p className="mb-3 text-text-secondary">
                            {result.alternativesIntro}
                        </p>
                    )}

                    {/* Alternativtabellen */}
                    <table className="w-full text-left text-sm">
                        <thead>
                        <tr className="border-b">
                            <th className="py-1">Bindningstid</th>
                            <th className="py-1">Snittränta</th>
                            <th className="py-1">Skillnad</th>
                            <th className="py-1">Årlig effekt</th>
                        </tr>
                        </thead>
                        <tbody>
                        {result.alternatives.map((alt, i) => (
                            <tr key={i} className="border-b">
                                <td className="py-1">{formatTerm(alt.term)}</td>
                                <td className="py-1">{formatPercent(alt.averageRate)}</td>
                                <td className="py-1">{formatPercent(alt.differenceFromBest)}</td>
                                <td className="py-1">{formatYearlyEffect(alt.yearlyCostDifference)}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

        </div>
    );
};

export default SmartRateTestResultView;