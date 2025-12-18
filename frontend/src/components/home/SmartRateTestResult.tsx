import type { FC } from "react";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import type { SmartRateStatus, SmartRateTestResult } from "../../types/smartRate";

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

/** Konsumentvänliga termer */
function formatTerm(term: string, t: TFunction): string {
    return t(`smartRate.result.terms.${term}`, term);
}

/** Procentformat max 2 decimals */
function formatPercent(value: number | null): string {
    if (value === null || value === undefined) return "—";
    const formatted = value.toFixed(2).replace(/\.00$/, "");
    return `${formatted} %`;
}

/** Årskostnad/årssparing */
function formatYearlyEffect(value: number | null, t: TFunction): string {
    if (value === null || value === undefined) return "—";
    const rounded = Math.round(value);
    if (rounded === 0) return t("smartRate.result.zeroPerYear");

    const abs = Math.abs(rounded).toLocaleString("sv-SE");
    return rounded > 0
        ? t("smartRate.result.moreExpensivePerYear", { value: abs })
        : t("smartRate.result.cheaperPerYear", { value: abs });
}

const SmartRateTestResultView: FC<Props> = ({ result }) => {
    const { t } = useTranslation();

    if (!result) return null;

    const isOfferFlow = result.isOfferFlow === true;
    const color = statusColors[result.status as SmartRateStatus] || "bg-gray-500";

    // ✅ Type-safe defaults
    const alternatives = result.alternatives ?? [];
    const offerAnalyses = result.offerAnalyses ?? [];

    return (
        <div className="flex flex-col gap-8 p-4 border border-border rounded-lg bg-white">

            {/* STATUS & RUBRIK */}
            <div>
                <h2 className="text-2xl font-bold text-text-primary mb-2">
                    {isOfferFlow
                        ? t("smartRate.result.offerTitle")
                        : t("smartRate.result.statusTitle")}
                </h2>

                <span
                    className={`inline-block px-4 py-1 rounded-full text-white text-sm font-medium ${color}`}
                >
                    {isOfferFlow
                        ? t(`smartRate.result.offerStatus.${result.status}`)
                        : t(`smartRate.result.status.${result.status}`)}
                </span>
            </div>

            {/* HUVUDANALYS */}
            <div className="text-text-secondary leading-relaxed space-y-3 whitespace-pre-line">
                <p>{result.analysisText}</p>

                {result.additionalContext && <p>{result.additionalContext}</p>}

                {result.recommendation && (
                    <p className="font-semibold text-primary mt-3">
                        {result.recommendation}
                    </p>
                )}
            </div>

            {/* PREFERENSRÅD */}
            {result.preferenceAdvice && (
                <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <h3 className="font-semibold mb-1">
                        {t("smartRate.result.preferenceTitle")}
                    </h3>
                    <p>{result.preferenceAdvice}</p>
                </div>
            )}

            {/* ALTERNATIVLISTA – EJ OFFER */}
            {!isOfferFlow && alternatives.length > 0 && (
                <div>
                    <h3 className="font-semibold mb-4 text-lg">
                        {t("smartRate.result.alternativesTitle")}
                    </h3>

                    {result.alternativesIntro && (
                        <p className="mb-4 text-gray-600 leading-relaxed">
                            {result.alternativesIntro}
                        </p>
                    )}

                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.table.term")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.table.avgRate")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.table.diff")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.table.yearly")}</th>
                            </tr>
                            </thead>
                            <tbody>
                            {alternatives.map((alt, i) => (
                                <tr key={i}>
                                    <td className="py-2 px-3">{formatTerm(alt.term, t)}</td>
                                    <td className="py-2 px-3">{formatPercent(alt.averageRate)}</td>
                                    <td className="py-2 px-3">{formatPercent(alt.differenceFromBest)}</td>
                                    <td className="py-2 px-3">
                                        {formatYearlyEffect(alt.yearlyCostDifference, t)}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* OFFERANALYS */}
            {isOfferFlow && offerAnalyses.length > 0 && (
                <div>
                    <h3 className="font-semibold mb-4 text-lg">
                        {t("smartRate.result.offerTableTitle")}
                    </h3>

                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.offerTable.term")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.offerTable.offeredRate")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.offerTable.diffBest")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.offerTable.status")}</th>
                                <th className="py-2 px-3 font-medium">{t("smartRate.result.offerTable.yearly")}</th>
                            </tr>
                            </thead>
                            <tbody>
                            {offerAnalyses.map((oa, i) => (
                                <tr key={i}>
                                    <td className="py-2 px-3">{formatTerm(oa.term, t)}</td>
                                    <td className="py-2 px-3">{formatPercent(oa.offeredRate)}</td>
                                    <td className="py-2 px-3">{formatPercent(oa.diffFromBestMarket)}</td>
                                    <td className="py-2 px-3">
                                            <span
                                                className={`px-2 py-1 rounded-lg text-white text-xs font-semibold ${statusColors[oa.status as SmartRateStatus]}`}
                                            >
                                                {t(`smartRate.result.offerStatus.${oa.status}`)}
                                            </span>
                                    </td>
                                    <td className="py-2 px-3">
                                        {formatYearlyEffect(oa.yearlyCostDifference, t)}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* CTA */}
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm leading-relaxed">
                <p className="mb-2">{t("smartRate.result.ctaText")}</p>
                <a
                    href="#rates"
                    className="inline-flex items-center text-blue-600 font-semibold hover:underline cursor-pointer"
                >
                    {t("smartRate.result.ctaLink")}
                </a>
                <p className="mt-3 text-gray-700">
                    {t("smartRate.result.ctaHint")}
                </p>
            </div>
        </div>
    );
};

export default SmartRateTestResultView;