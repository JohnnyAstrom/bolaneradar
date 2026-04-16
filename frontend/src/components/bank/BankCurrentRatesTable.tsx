/**
 * BankCurrentRatesTable.tsx
 *
 * Mobil:
 *  - Kolumner: Bindningstid | Listränta | Senast ändrad | Snittränta
 *  - Klick på Listränta visar förändring (▲/▼)
 *
 * Desktop:
 *  - Full tabell med separat kolumn för Förändring
 */

import type { FC } from "react";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import type { BankRateRow } from "../../services/bankApi";

interface Props {
    rows: BankRateRow[];
    averageMonthFormatted: string | null;
}

// Rubriker
const makeHeaders = (
    t: (key: string, options?: any) => string,
    averageMonthFormatted: string | null
) => [
    { desktop: t("bank.rates.headers.term"), mobile: t("bank.rates.headers.term") },
    { desktop: t("bank.rates.headers.listRate"), mobile: t("bank.rates.headers.listRate") },
    { desktop: t("bank.rates.headers.change"), mobile: t("bank.rates.headers.lastChanged") },
    { desktop: t("bank.rates.headers.lastChanged"), mobile: "" }, // dold på mobil
    {
        desktop: averageMonthFormatted
            ? t("bank.rates.headers.avgRateWithMonth", { month: averageMonthFormatted })
            : t("bank.rates.headers.avgRate"),
        mobile: averageMonthFormatted
            ? t("bank.rates.headers.avgRateWithMonthMobile", { month: averageMonthFormatted })
            : t("bank.rates.headers.avgRate"),
    }
];

const BankCurrentRatesTable: FC<Props> = ({ rows, averageMonthFormatted }) => {
    const { t } = useTranslation();
    const [openIndex, setOpenIndex] = useState<number | null>(null);

    const toggleMobileRow = (i: number) => {
        setOpenIndex(prev => (prev === i ? null : i));
    };

    return (
        <div>
            <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h2 className="text-2xl font-semibold text-text-primary">
                        {t("bank.rates.title")}
                    </h2>
                    <p className="mt-2 max-w-2xl text-sm leading-6 text-text-secondary">
                        {t("bank.rates.intro")}
                    </p>
                </div>
            </div>

            <div className="overflow-x-auto rounded-[24px] border border-slate-200 bg-white shadow-sm">
                <table className="min-w-full">

                    {/* Headers */}
                    <thead className="bg-slate-50/90 text-text-primary">
                    <tr className="border-b border-slate-200">
                        {makeHeaders(t, averageMonthFormatted).map((h, index) => (
                            <th
                                key={index}
                                className={`
                                        px-2 sm:px-4 py-3 text-left whitespace-nowrap
                                        ${index === 3 ? "hidden md:table-cell" : ""}
                                    `}
                            >
                                    <span className="hidden md:inline text-xs font-semibold uppercase tracking-[0.04em] text-slate-600">
                                        {h.desktop}
                                    </span>
                                <span className="md:hidden whitespace-pre-line text-[11px] font-semibold uppercase leading-tight tracking-[0.04em] text-slate-600">
                                {h.mobile}
                            </span>
                            </th>
                        ))}
                    </tr>
                    </thead>

                    <tbody className="text-text-primary">
                    {rows.map((row, index) => {
                        const diff = row.change;
                        const isOpen = openIndex === index;

                        const rateClass =
                            diff == null
                                ? "bg-gray-100 text-gray-700"
                                : diff < 0
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700";

                        return (
                            <React.Fragment key={row.term ?? index}>
                                {/* Huvudrad */}
                                <tr className="border-b border-slate-100 last:border-0 hover:bg-slate-50/80 transition-colors">

                                    {/* Bindningstid */}
                                    <td className="px-2 sm:px-4 py-3.5">

                                        {/* Desktop – full text */}
                                        <span className="hidden font-medium sm:inline">
                                            {t(`mortgage.term.${row.term}`, row.term)}
                                        </span>

                                        {/* Mobil – kort text */}
                                        <span className="font-medium sm:hidden">
                                            {t(`mortgage.termShort.${row.term}`, row.term)}
                                        </span>
                                    </td>

                                    {/* Listränta – klickbar på mobil */}
                                    <td
                                        className="px-0 sm:px-4 py-3.5 cursor-pointer md:cursor-default"
                                        onClick={() => toggleMobileRow(index)}
                                    >
                                        {row.currentRate != null ? (
                                            <div className="flex items-center gap-1">
                                                    <span
                                                        className={`
                                                            inline-flex items-center gap-1
                                                            px-2 h-[28px] rounded-xl
                                                            text-xs sm:text-sm font-medium
                                                            ${rateClass}
                                                        `}
                                                    >
                                                        {row.currentRate}%
                                                        {diff != null && (diff < 0 ? " ▼" : " ▲")}
                                                    </span>

                                                {/* Chevron – endast mobil */}
                                                <span className="md:hidden text-gray-400 text-xl">
                                                        ›
                                                    </span>
                                            </div>
                                        ) : "–"}
                                    </td>

                                    {/* Förändring – desktop */}
                                    <td className="px-2 sm:px-4 py-3.5 hidden md:table-cell">
                                        {diff == null ? "–" : (
                                            <span
                                                className={`
                                                        inline-flex items-center gap-1
                                                        px-2.5 h-[28px] rounded-xl
                                                        text-xs sm:text-sm font-medium
                                                        ${diff < 0
                                                    ? "bg-green-100 text-green-700"
                                                    : "bg-red-100 text-red-700"}
                                                    `}
                                            >
                                                    {diff < 0 ? "▼" : "▲"} {Math.abs(diff).toFixed(2)}%
                                                </span>
                                        )}
                                    </td>

                                    {/* Senast ändrad */}
                                    <td className="px-2 sm:px-4 py-3.5 text-sm text-slate-700">
                                        {row.lastChanged ?? "–"}
                                    </td>

                                    {/* Snittränta */}
                                    <td className="px-2 sm:px-4 py-3.5 font-medium">
                                        {row.avgRate != null ? `${row.avgRate}%` : "–"}
                                    </td>


                                </tr>

                                {/* Extra rad – mobil: Förändring */}
                                {isOpen && (
                                    <tr className="md:hidden bg-gray-50">
                                        <td
                                            colSpan={4}
                                            className="px-4 py-2 text-xs text-text-secondary"
                                        >
                                                <span className="font-medium text-text-primary">
                                                    {t("bank.rates.headers.change")}:
                                                </span>{" "}
                                            {diff == null
                                                ? "–"
                                                : `${diff < 0 ? "▼" : "▲"} ${Math.abs(diff).toFixed(2)}%`}
                                        </td>
                                    </tr>
                                )}
                            </React.Fragment>
                        );
                    })}
                    </tbody>

                </table>
            </div>
        </div>
    );
};

export default BankCurrentRatesTable;
