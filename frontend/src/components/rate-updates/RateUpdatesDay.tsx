import {useTranslation} from "react-i18next";
import type {RateUpdateDay, RateUpdate} from "../../types/rateUpdates";
import {RateUpdateRow} from "./RateUpdateRow";

export function RateUpdatesDay({day}: { day: RateUpdateDay }) {
    const {t, i18n} = useTranslation();
    const groupedByBank = groupByBank(day.updates);
    const bankEntries = Object.entries(groupedByBank);

    return (
        <div className="border border-gray-200 rounded-3xl bg-white px-4 py-4 shadow-sm sm:px-5 lg:px-6 lg:py-5">
            {/* Datum */}
            <h2 className="text-sm font-semibold text-gray-700 mb-3">
                {formatDate(day.date, i18n.language)}
            </h2>

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                {bankEntries.map(([bankName, updates]) => (
                    <div
                        key={bankName}
                        className={`rounded-2xl border border-slate-100 bg-slate-50/55 px-4 py-4 ${
                            bankEntries.length === 1 ? "lg:max-w-[460px]" : ""
                        }`}
                    >
                        {/* Banknamn */}
                        <h3 className="mb-3 font-semibold text-gray-900">
                            {bankName}
                        </h3>

                        {/* Kolumnrubriker */}
                        <div
                            className="
                            grid
                            grid-cols-[minmax(0,0.95fr)_0.95fr_0.95fr_0.95fr]
                            w-full
                            gap-x-2
                            sm:grid-cols-[minmax(0,1fr)_88px_88px_88px]
                            lg:grid-cols-[minmax(0,1fr)_96px_96px_96px]
                            text-[11px] font-medium text-gray-700
                            sm:text-xs
                            mb-2 pl-0
                        "
                        >
                            {/* Bindningstid */}
                            <span className="min-w-0">
                                <span className="sm:hidden">
                                    {t("rateUpdates.columns.termShort")}
                                </span>
                                <span className="hidden sm:inline">
                                    {t("rateUpdates.columns.term")}
                                </span>
                            </span>

                            {/* Tidigare ränta */}
                            <span className="text-center">
                                <span className="sm:hidden">
                                    {t("rateUpdates.columns.beforeShort")}
                                </span>
                                <span className="hidden sm:inline">
                                    {t("rateUpdates.columns.before")}
                                </span>
                            </span>

                            {/* Förändring */}
                            <span className="text-center">
                                {t("rateUpdates.columns.change")}
                            </span>

                            {/* Ny ränta */}
                            <span className="text-center">
                                <span className="sm:hidden">
                                    {t("rateUpdates.columns.afterShort")}
                                </span>
                                <span className="hidden sm:inline">
                                    {t("rateUpdates.columns.after")}
                                </span>
                            </span>
                        </div>

                        {/* Rader */}
                        <ul className="flex flex-col gap-0.5">
                            {updates.map((update, index) => (
                                <RateUpdateRow
                                    key={index}
                                    update={update}
                                />
                            ))}
                        </ul>
                    </div>
                ))}
            </div>
        </div>
    );
}

/* =========================
 * Hjälpfunktioner
 * ========================= */

function groupByBank(updates: RateUpdate[]) {
    return updates.reduce<Record<string, RateUpdate[]>>((acc, update) => {
        acc[update.bankName] ||= [];
        acc[update.bankName].push(update);
        return acc;
    }, {});
}

function formatDate(date: string, language: string) {
    const locale = language === "en" ? "en-GB" : "sv-SE";

    return new Date(date).toLocaleDateString(locale, {
        year: "numeric",
        month: "long",
        day: "numeric",
    });
}
