import {type FC } from "react";
import {useTranslation} from "react-i18next";
import type {TFunction} from "i18next";
import type {SmartRateStatus, SmartRateTestResult} from "../../types/smartRate";
import { ArrowRight, BadgeInfo, CircleDollarSign, FileSearch, Target } from "lucide-react";

interface Props {
    result: SmartRateTestResult | null | undefined;
    onScrollToRates: () => void;
}

const statusColors: Record<SmartRateStatus, string> = {
    GREAT_GREEN: "bg-green-600",
    GREEN: "bg-green-500",
    YELLOW: "bg-yellow-500 text-slate-950",
    ORANGE: "bg-orange-500",
    RED: "bg-red-500",
    INFO: "bg-blue-500",
    UNKNOWN: "bg-gray-500"
};

const statusPanelColors: Record<SmartRateStatus, string> = {
    GREAT_GREEN: "border-green-200 bg-green-50",
    GREEN: "border-green-200 bg-green-50",
    YELLOW: "border-yellow-200 bg-yellow-50",
    ORANGE: "border-orange-200 bg-orange-50",
    RED: "border-red-200 bg-red-50",
    INFO: "border-blue-200 bg-blue-50",
    UNKNOWN: "border-slate-200 bg-slate-50"
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
            <section className={`rounded-[28px] border p-5 shadow-sm sm:p-6 ${statusPanelColors[result.status as SmartRateStatus] || "border-slate-200 bg-white"}`}>
                <div className="max-w-2xl">
                        <h2 className="text-3xl font-bold tracking-tight text-text-primary mb-3">
                            {isOfferFlow
                                ? t("smartRate.result.offerTitle")
                                : t("smartRate.result.statusTitle")}
                        </h2>

                        <span
                            className={`inline-flex items-center rounded-full px-4 py-1.5 text-sm font-semibold text-white ${color}`}
                        >
                            {isOfferFlow
                                ? t(`smartRate.result.offerStatus.${result.status}`)
                                : t(`smartRate.result.status.${result.status}`)}
                        </span>
                </div>

                <div className="mt-6 grid gap-4 lg:grid-cols-[1.3fr_0.7fr]">
                    <div className="rounded-3xl border border-white/70 bg-white/60 p-5">
                        <div className="text-xs font-semibold uppercase tracking-[0.14em] text-text-secondary mb-3">
                            Analys
                        </div>
                        <div className="space-y-4">
                            <div className="text-[1.02rem] leading-8 text-slate-800">
                                {result.analysisText}
                            </div>

                            {result.additionalContext && (
                                <div className="border-t border-slate-200/80 pt-4">
                                    <p className="text-sm leading-7 text-slate-700">
                                        {result.additionalContext}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="flex flex-col gap-4">
                        {result.recommendation && (
                            <div className="rounded-3xl border border-primary/20 bg-white/75 p-5">
                                <div className="text-xs font-semibold uppercase tracking-[0.14em] text-primary mb-3">
                                    Rekommendation
                                </div>
                                <p className="text-sm leading-7 font-semibold text-primary">
                                    {result.recommendation}
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </section>

            {result.preferenceAdvice && (
                <section className="rounded-3xl border border-sky-200 bg-sky-50/75 p-5 sm:p-6">
                    <div className="flex items-start gap-3">
                        <div className="mt-0.5 text-primary">
                            <Target size={18} />
                        </div>
                        <div>
                            <h3 className="font-semibold mb-2 text-text-primary">
                                {t("smartRate.result.preferenceTitle")}
                            </h3>
                            <p className="max-w-4xl leading-7 text-slate-700">{result.preferenceAdvice}</p>
                        </div>
                    </div>
                </section>
            )}

            {!isOfferFlow && alternatives.length > 0 && (
                <section className="rounded-3xl border border-border bg-white p-5 shadow-sm sm:p-6">
                    <div className="flex items-start gap-3 mb-4">
                        <div className="mt-0.5 text-primary">
                            <CircleDollarSign size={18} />
                        </div>
                        <div>
                            <h3 className="font-semibold text-lg text-text-primary">
                                {t("smartRate.result.alternativesTitle")}
                            </h3>

                            {result.alternativesIntro && (
                                <p className="mt-2 text-gray-600 leading-relaxed">
                                    {result.alternativesIntro}
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="hidden md:block overflow-hidden rounded-2xl border border-slate-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-slate-50/80 text-gray-700 border-b">
                            <tr>
                                <th className="py-2 px-3">{t("smartRate.result.table.term")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.avgRate")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.diff")}</th>
                                <th className="py-2 px-3">{t("smartRate.result.table.yearly")}</th>
                            </tr>
                            </thead>
                            <tbody>
                            {alternatives.map((alt, i) => (
                                <tr key={i} className="border-t border-slate-100">
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

                    <div className="md:hidden space-y-3">
                        {alternatives.map((alt, i) => (
                            <div key={i} className="border border-slate-200 rounded-2xl p-4">
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

            {isOfferFlow && offerAnalyses.length > 0 && (
                <section className="rounded-3xl border border-border bg-white p-5 shadow-sm sm:p-6">
                    <div className="flex items-start gap-3 mb-4">
                        <div className="mt-0.5 text-primary">
                            <FileSearch size={18} />
                        </div>
                        <div>
                            <h3 className="font-semibold text-lg text-text-primary">
                                {t("smartRate.result.offerTableTitle")}
                            </h3>
                        </div>
                    </div>

                    <div className="hidden md:block overflow-hidden rounded-2xl border border-slate-200">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-slate-50 text-gray-700 border-b">
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
                                <tr key={i} className="border-t border-slate-100">
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

                    <div className="md:hidden space-y-3">
                        {offerAnalyses.map((oa, i) => (
                            <div key={i} className="border border-slate-200 rounded-2xl p-4">
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

                <section className="rounded-3xl border border-blue-200 bg-blue-50/55 p-5 text-sm sm:p-6">
                <div className="flex items-start gap-3">
                    <div className="mt-0.5 text-primary">
                        <BadgeInfo size={18} />
                    </div>
                    <div>
                        <p className="mb-3 leading-7 text-slate-700">
                            {t("smartRate.result.ctaText")}
                        </p>

                        <button
                            type="button"
                            onClick={onScrollToRates}
                            className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 font-semibold text-blue-700 shadow-sm transition-colors hover:bg-blue-100"
                        >
                            <ArrowRight size={16} />
                            {t("smartRate.result.ctaLink")}
                        </button>

                        <p className="mt-3 leading-7 text-gray-600">
                            {t("smartRate.result.ctaHint")}
                        </p>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default SmartRateTestResultView;
