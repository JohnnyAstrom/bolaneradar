/**
 * BankCurrentRatesTable.tsx
 *
 * Mobilvänlig tabell:
 *  - Kortare rubriker
 *  - Dold datum-kolumn på mobil
 *  - Klick på "Förändring" öppnar extra rad
 */

import type { FC } from "react";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import type { BankRateRow } from "../../client/bankApi";

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
    {
        desktop: t("bank.rates.headers.change"),
        mobile: t("bank.rates.headers.changeMobile")
    },
    {
        desktop: averageMonthFormatted
            ? t("bank.rates.headers.avgRateWithMonth", { month: averageMonthFormatted })
            : t("bank.rates.headers.avgRate"),
        mobile: averageMonthFormatted
            ? t("bank.rates.headers.avgRateWithMonthMobile", { month: averageMonthFormatted })
            : t("bank.rates.headers.avgRate"),
    },
    { desktop: t("bank.rates.headers.lastChanged"), mobile: t("bank.rates.headers.lastChangedShort") },
];

const BankCurrentRatesTable: FC<Props> = ({ rows, averageMonthFormatted }) => {
    const { t } = useTranslation();
    const [openIndex, setOpenIndex] = useState<number | null>(null);

    const toggleMobileRow = (i: number) => {
        setOpenIndex((prev) => (prev === i ? null : i));
    };

    return (
        <div>
            <h2 className="text-2xl font-semibold text-text-primary mb-5">
                {t("bank.rates.title")}
            </h2>

            <div className="overflow-x-auto border border-border rounded-lg bg-white">
                <table className="min-w-full">

                    {/* Kolumnrubriker */}
                    <thead className="bg-bg-light text-text-primary">
                    <tr>
                        {makeHeaders(t, averageMonthFormatted).map((h, index) => (
                            <th
                                key={index}
                                className={`
                                    px-2 sm:px-4 py-2 text-left whitespace-nowrap
                                    ${index === 4 ? "hidden md:table-cell" : ""}
                                `}
                            >
                                {/* Desktop */}
                                <span className="hidden md:inline text-sm">
                                    {h.desktop}
                                </span>

                                {/* Mobil */}
                                <span className="md:hidden whitespace-pre-line text-[11px] leading-tight">
                                    {h.mobile}
                                </span>
                            </th>
                        ))}
                    </tr>
                    </thead>

                    <tbody className="text-text-primary">

                    {rows.map((row, index) => {
                        const diff = row.change;

                        const rateClass =
                            diff == null
                                ? "bg-gray-100 text-gray-700"
                                : diff < 0
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700";

                        const isOpen = openIndex === index;

                        return (
                            <React.Fragment key={row.term ?? index}>
                                {/* ---- HUVUDRAD ---- */}
                                <tr className="hover:bg-row-hover transition-colors">

                                    {/* Bindningstid */}
                                    <td className="px-2 sm:px-4 py-3">
                                        {row.term}
                                    </td>

                                    {/* Listränta */}
                                    <td className="px-2 sm:px-4 py-3">
                                        {row.currentRate !== null ? (
                                            <span
                                                className={`inline-flex items-center gap-1 px-2 h-[26px] rounded-lg text-xs sm:text-sm font-medium ${rateClass}`}
                                            >
                                                {row.currentRate}%
                                            </span>
                                        ) : "–"}
                                    </td>

                                    {/* Förändring — KLICKBAR PÅ MOBIL */}
                                    <td
                                        className="px-2 sm:px-4 py-3 cursor-pointer md:cursor-default"
                                        onClick={() => toggleMobileRow(index)}
                                    >
                                        {diff == null ? "–" : (
                                            <span
                                                className={`
                                                    inline-flex items-center gap-1
                                                    px-2 h-[26px] rounded-lg 
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

                                    {/* Snittränta */}
                                    <td className="px-2 sm:px-4 py-3">
                                        {row.avgRate !== null ? `${row.avgRate}%` : "–"}
                                    </td>

                                    {/* Senast ändrad — desktop only */}
                                    <td className="px-2 sm:px-4 py-3 hidden md:table-cell">
                                        {row.lastChanged ?? "–"}
                                    </td>
                                </tr>

                                {/* ---- MOBIL: EXTRA RAD ---- */}
                                {isOpen && (
                                    <tr className="md:hidden bg-gray-50">
                                        <td
                                            colSpan={4}
                                            className="px-4 py-2 text-xs text-text-secondary"
                                        >
                                            <span className="font-medium text-text-primary">
                                                {t("bank.rates.lastChangedLabel")}
                                            </span>{" "}
                                            {row.lastChanged ?? "–"}
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