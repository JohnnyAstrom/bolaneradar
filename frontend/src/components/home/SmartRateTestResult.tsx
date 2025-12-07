import type { FC } from "react";
import type {SmartRateStatus, SmartRateTestResult} from "../../types/smartRate";

interface Props {
    result: SmartRateTestResult | null | undefined;
}

const statusColors: Record<SmartRateStatus, string> = {
    GREAT_GREEN: "bg-green-600",
    GREEN: "bg-green-500",
    YELLOW: "bg-yellow-500",
    ORANGE: "bg-orange-500",
    RED: "bg-red-500",
    INFO: "bg-blue-500",
    UNKNOWN: "bg-gray-500"
};

const statusLabels: Record<SmartRateStatus, string> = {
    GREAT_GREEN: "Ovanligt bra ränta!",
    GREEN: "Bra ränta!",
    YELLOW: "Lite hög ränta!",
    ORANGE: "Hög ränta!",
    RED: "Mycket hög ränta!",
    INFO: "Informativt läge",
    UNKNOWN: "Okänt läge"
};

const offerStatusLabels: Record<SmartRateStatus, string> = {
    GREAT_GREEN: "Mycket bra erbjudande",
    GREEN: "Bra erbjudande",
    YELLOW: "Helt okej erbjudande",
    ORANGE: "Svagt erbjudande",
    RED: "Dåligt erbjudande",
    INFO: "Informativt läge",
    UNKNOWN: "Okänt läge"
};


/** Konsumentvänliga termer */
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

/** Procentformat max 2 decimals */
function formatPercent(value: number | null): string {
    if (value === null || value === undefined) return "—";
    const formatted = value.toFixed(2).replace(/\.00$/, "");
    return `${formatted} %`;
}

/** Årskostnad/årssparing */
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

    const color = statusColors[result.status as SmartRateStatus] || "bg-gray-500";

    // Enkelt: isOfferFlow = "Har du fått ett ränteerbjudande? Ja"
    const isOfferFlow = result.isOfferFlow === true;

    return (
        <div className="flex flex-col gap-8 p-4 border border-border rounded-lg bg-white">

            {/* =============================== */}
            {/*  STATUS & RUBRIK */}
            {/* =============================== */}
            <div>
                <h2 className="text-2xl font-bold text-text-primary mb-2">
                    {
                        isOfferFlow
                        ? "Analys av dina ränteerbjudanden"
                        : "Din räntestatus"
                    }
                </h2>

                <span
                    className={`inline-block px-4 py-1 rounded-full text-white text-sm font-medium ${color}`}
                >
                    {
                        isOfferFlow
                        ? offerStatusLabels[result.status as SmartRateStatus]
                        : statusLabels[result.status as SmartRateStatus]
                    }
                </span>
            </div>

            {/* =============================== */}
            {/*  HUVUDANALYS */}
            {/* =============================== */}
            <div className="text-text-secondary leading-relaxed space-y-3 whitespace-pre-line">
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
            {/*  ALTERNATIVLISTA → ENDAST NÄR hasOffer = NEJ */}
            {/* =============================== */}
            {!isOfferFlow && result.alternatives && result.alternatives.length > 0 && (
                <div>
                    <h3 className="font-semibold mb-4 text-lg">
                        Räntor som passar din valda bindningstid
                    </h3>

                    {result.alternativesIntro && (
                        <p className="mb-4 text-gray-600 leading-relaxed">
                            {result.alternativesIntro}
                        </p>
                    )}

                    {/* DESKTOP TABELL */}
                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3 font-medium">Bindningstid</th>
                                <th className="py-2 px-3 font-medium">Marknadens snittränta:</th>
                                <th className="py-2 px-3 font-medium">Skillnad mot din nuvarande:</th>
                                <th className="py-2 px-3 font-medium">Årlig kostnadsskillnad:</th>
                            </tr>
                            </thead>

                            <tbody>
                            {result.alternatives.map((alt, i) => (
                                <tr
                                    key={i}
                                    className={`border-b last:border-0 hover:bg-gray-50 transition ${
                                        i % 2 === 0 ? "bg-white" : "bg-gray-50/60"
                                    }`}
                                >
                                    <td className="py-2 px-3">{formatTerm(alt.term)}</td>
                                    <td className="py-2 px-3">{formatPercent(alt.averageRate)}</td>
                                    <td className="py-2 px-3">{formatPercent(alt.differenceFromBest)}</td>
                                    <td className="py-2 px-3">{formatYearlyEffect(alt.yearlyCostDifference)}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* MOBILE CARDS — ALTERNATIVES */}
                    <div className="md:hidden flex flex-col gap-5">
                        {result.alternatives.map((alt, i) => (
                            <div
                                key={i}
                                className={`
                                    p-5 rounded-xl border shadow-sm
                                    ${i % 2 === 0 ? "bg-white" : "bg-gray-50"}
                                `}
                            >
                                {/* HEADER */}
                                <div className="flex justify-between items-center mb-3">
                                    <div className="text-base font-semibold">
                                        {formatTerm(alt.term)}
                                    </div>
                                </div>

                                <div className="h-px bg-gray-200 mb-3"></div>

                                {/* BODY */}
                                <div className="space-y-2 text-sm text-gray-800">
                                    <div>
                                        <span className="font-medium">Marknadens snittränta:</span>
                                        {" "}{formatPercent(alt.averageRate)}
                                    </div>

                                    <div>
                                        <span className="font-medium">Skillnad mot din nuvarande:</span>
                                        {" "}{formatPercent(alt.differenceFromBest)}
                                    </div>

                                    <div>
                                        <span className="font-medium">Årlig kostnadsskillnad:</span>
                                        {" "}{formatYearlyEffect(alt.yearlyCostDifference)}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}


            {/* =============================== */}
            {/*  TABELL → ENDAST NÄR hasOffer = JA */}
            {/* =============================== */}
            {isOfferFlow && result.offerAnalyses && result.offerAnalyses.length > 0 && (
                <div>
                    <h3 className="font-semibold mb-4 text-lg">
                        Analys av dina ränteerbjudanden
                    </h3>

                    {/* DESKTOP TABELL */}
                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3 font-medium">Bindningstid</th>
                                <th className="py-2 px-3 font-medium">Erbjuden ränta</th>
                                <th className="py-2 px-3 font-medium">Skillnad mot bästa</th>
                                <th className="py-2 px-3 font-medium">Status</th>
                                <th className="py-2 px-3 font-medium">Årlig kostnadsskillnad</th>
                            </tr>
                            </thead>

                            <tbody>
                            {result.offerAnalyses.map((oa, i) => (
                                <tr
                                    key={i}
                                    className={`border-b last:border-0 hover:bg-gray-50 transition ${
                                        i % 2 === 0 ? "bg-white" : "bg-gray-50/60"
                                    }`}
                                >
                                    <td className="py-2 px-3">{formatTerm(oa.term)}</td>
                                    <td className="py-2 px-3">{formatPercent(oa.offeredRate)}</td>
                                    <td className="py-2 px-3">{formatPercent(oa.diffFromBestMarket)}</td>

                                    <td className="py-2 px-3">
                            <span
                                className={`px-2 py-1 rounded-lg text-white text-xs font-semibold ${statusColors[oa.status as SmartRateStatus]}`}
                            >
                                {
                                    isOfferFlow
                                        ? offerStatusLabels[oa.status as SmartRateStatus]   // använd offer-labels
                                        : statusLabels[oa.status as SmartRateStatus]        // använd vanliga labels
                                }
                            </span>
                                    </td>

                                    <td className="py-2 px-3">{formatYearlyEffect(oa.yearlyCostDifference)}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* MOBILE CARDS */}
                    <div className="md:hidden flex flex-col gap-5">
                        {result.offerAnalyses.map((oa, i) => (
                            <div
                                key={i}
                                className={`
                                    p-5 rounded-xl border shadow-sm 
                                    ${i % 2 === 0 ? "bg-white" : "bg-gray-50"}
                                `}
                            >
                                {/* HEADER */}
                                <div className="flex justify-between items-center mb-3">
                                    <div className="text-base font-semibold">
                                        {formatTerm(oa.term)}
                                    </div>

                                    <span
                                        className={`
                                            px-2.5 py-1 rounded-lg text-white text-xs font-semibold
                                            ${statusColors[oa.status as SmartRateStatus]}
                                        `}
                                    >
                                        {
                                            isOfferFlow
                                                ? offerStatusLabels[oa.status as SmartRateStatus]   // använd offer-labels
                                                : statusLabels[oa.status as SmartRateStatus]        // använd vanliga labels
                                        }
                                    </span>
                                </div>

                                <div className="h-px bg-gray-200 mb-3"></div>

                                {/* BODY */}
                                <div className="space-y-2 text-sm text-gray-800">
                                    <div>
                                        <span className="font-medium">Erbjuden ränta:</span>
                                        {" "}{formatPercent(oa.offeredRate)}
                                    </div>

                                    <div>
                                        <span className="font-medium">Skillnad mot bästa:</span>
                                        {" "}{formatPercent(oa.diffFromBestMarket)}
                                    </div>

                                    <div>
                                        <span className="font-medium">Årlig kostnadsskillnad:</span>
                                        {" "}{formatYearlyEffect(oa.yearlyCostDifference)}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* CTA – nästa steg */}
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm leading-relaxed">
                <p className="mb-2">
                    Nu när du vet hur din nivå står sig mot marknaden
                    kan du utforska aktuella snitträntor och jämföra alla banker i detalj.
                </p>

                <a
                    href="#rates"
                    className="inline-flex items-center text-blue-600 font-semibold hover:underline cursor-pointer"
                >
                    👉 Klicka här för att gå till snitträntetabellen
                </a>



                <p className="mt-3 text-gray-700">
                    💡 Du kan också klicka på varje bank för att se deras historik,
                    bindningstider och mer information.
                </p>
            </div>
        </div>
    );
};

export default SmartRateTestResultView;