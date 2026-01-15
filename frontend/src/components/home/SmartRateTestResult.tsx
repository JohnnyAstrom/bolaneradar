import {type FC } from "react";
import {useTranslation} from "react-i18next";
import type {TFunction} from "i18next";
import type {SmartRateStatus, SmartRateTestResult} from "../../types/smartRate";

interface Props {
    result: SmartRateTestResult | null | undefined;
    onScrollToRates: () => void;
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
        ? t("smartRate.result.moreExpensivePerYear", {value: abs})
        : t("smartRate.result.cheaperPerYear", {value: abs});
}

const SmartRateTestResultView: FC<Props> = ({result, onScrollToRates}) => {
    const {t} = useTranslation();
    if (!result) return null;

    const isOfferFlow = result.isOfferFlow === true;
    const color = statusColors[result.status as SmartRateStatus] || "bg-gray-500";

    const alternatives = result.alternatives ?? [];
    const offerAnalyses = result.offerAnalyses ?? [];

    return (
        <div className="flex flex-col gap-6">

            {/* ================= STATUS + ANALYS ================= */}
            <section className="p-4 rounded-lg bg-white border border-border space-y-4">

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

                <div className="text-sm text-text-secondary leading-relaxed space-y-3 whitespace-pre-line">
                    <p>{result.analysisText}</p>

                    {result.additionalContext && <p>{result.additionalContext}</p>}

                    {result.recommendation && (
                        <p className="font-semibold text-primary">
                            {result.recommendation}
                        </p>
                    )}
                </div>

            </section>


            {/* ================= PREFERENS ================= */}
            {result.preferenceAdvice && (
                <section className="p-4 rounded-lg bg-blue-50 border border-blue-200">
                    <h3 className="font-semibold mb-1">
                        {t("smartRate.result.preferenceTitle")}
                    </h3>
                    <p>{result.preferenceAdvice}</p>
                </section>
            )}

            {/* ================= ALTERNATIV ================= */}
            {!isOfferFlow && alternatives.length > 0 && (
                <section className="p-4 rounded-lg bg-white border border-border">
                    <h3 className="font-semibold mb-4 text-lg">
                        {t("smartRate.result.alternativesTitle")}
                    </h3>

                    {result.alternativesIntro && (
                        <p className="mb-4 text-gray-600 leading-relaxed">
                            {result.alternativesIntro}
                        </p>
                    )}

                    {/* Desktop table */}
                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3">{t("smartRate.result.table.term")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.avgRate")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.diff")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.yearly")}</th>
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

                    {/* Mobile cards */}
                    <div className="md:hidden space-y-3">
                        {alternatives.map((alt, i) => (
                            <div key={i} className="border border-gray-200 rounded-lg p-3">
                                <div className="font-semibold mb-1">
                                    {formatTerm(alt.term, t)}
                                </div>
                                <div className="text-sm space-y-1">
                                    <div>
                                        {t("smartRate.result.table.avgRate")}:{" "}
                                        <strong>{formatPercent(alt.averageRate)}</strong>
                                    </div>
                                    <div>
                                        {t("smartRate.result.table.diff")}:{" "}
                                        {formatPercent(alt.differenceFromBest)}
                                    </div>
                                    <div>
                                        {formatYearlyEffect(alt.yearlyCostDifference, t)}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {/* ================= OFFER ================= */}
            {isOfferFlow && offerAnalyses.length > 0 && (
                <section className="p-4 rounded-lg bg-white border border-border">
                    <h3 className="font-semibold mb-4 text-lg">
                        {t("smartRate.result.offerTableTitle")}
                    </h3>

                    {/* Desktop table */}
                    <div className="hidden md:block overflow-hidden rounded-lg border border-gray-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3">{t("smartRate.result.offerTable.term")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.offerTable.offeredRate")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.offerTable.diffBest")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.offerTable.status")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.offerTable.yearly")}</th>
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
                                                className={`px-2 py-1 rounded-lg text-white text-sm font-semibold ${statusColors[oa.status as SmartRateStatus]}`}
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

                    {/* Mobile cards */}
                    <div className="md:hidden space-y-3">
                        {offerAnalyses.map((oa, i) => (
                            <div key={i} className="border border-gray-200 rounded-lg p-3">
                                <div className="font-semibold mb-1">
                                    {formatTerm(oa.term, t)}
                                </div>
                                <div className="text-sm space-y-1">
                                    <div>
                                        {t("smartRate.result.offerTable.offeredRate")}:{" "}
                                        <strong>{formatPercent(oa.offeredRate)}</strong>
                                    </div>
                                    <div>
                                        {t("smartRate.result.offerTable.diffBest")}:{" "}
                                        {formatPercent(oa.diffFromBestMarket)}
                                    </div>
                                    <div>
                                        {formatYearlyEffect(oa.yearlyCostDifference, t)}
                                    </div>
                                </div>
                                <div className="mt-2">
                                    <span
                                        className={`inline-block px-2 py-1 rounded-lg text-white text-sm font-semibold ${statusColors[oa.status as SmartRateStatus]}`}
                                    >
                                        {t(`smartRate.result.offerStatus.${oa.status}`)}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {/* ================= CTA ================= */}
            <section className="p-4 rounded-lg bg-blue-50 border border-blue-200 text-sm">
                <p className="mb-2">
                    {t("smartRate.result.ctaText")}
                </p>

                <button
                    type="button"
                    onClick={onScrollToRates}
                    className="inline-flex items-center text-blue-600 font-semibold hover:underline"
                >
                    {t("smartRate.result.ctaLink")}
                </button>

                <p className="mt-3 text-gray-700">
                    {t("smartRate.result.ctaHint")}
                </p>
            </section>
        </div>
    );
};

export default SmartRateTestResultView;