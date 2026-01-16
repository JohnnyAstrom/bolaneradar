import {useTranslation} from "react-i18next";
import type {RateUpdateDay, RateUpdate} from "../../types/rateUpdates";
import {RateUpdateRow} from "./RateUpdateRow";

export function RateUpdatesDay({day}: { day: RateUpdateDay }) {
    const {t, i18n} = useTranslation();
    const groupedByBank = groupByBank(day.updates);

    return (
        <div className="border border-gray-200 rounded-lg bg-white px-4 py-4">
            {/* Datum */}
            <h2 className="text-sm font-semibold text-gray-700 mb-3">
                {formatDate(day.date, i18n.language)}
            </h2>

            <div className="flex flex-col gap-4">
                {Object.entries(groupedByBank).map(([bankName, updates]) => (
                    <div
                        key={bankName}
                        className="pb-3 mb-3 border-b border-gray-100 last:border-0"
                    >
                        {/* Banknamn */}
                        <h3 className="font-medium text-gray-900 mb-2">
                            {bankName}
                        </h3>

                        {/* Kolumnrubriker */}
                        <div
                            className="
                            grid
                            grid-cols-[0.8fr_1fr_1fr_1fr]
                            sm:grid-cols-[90px_110px_110px_100px]
                            text-xs font-medium text-gray-800
                            mb-1 pl-0
                        "
                        >
                            {/* Bindningstid */}
                            <span>
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
