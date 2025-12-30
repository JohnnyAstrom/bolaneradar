import type { FC } from "react";
import React, { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { bankDisplayNames } from "../../config/bankDisplayNames";
import { apiGet } from "../../client/client";

// ============================================================
// bankName → urlKey (inverterar displaynames)
// ============================================================
const bankNameToKey = Object.fromEntries(
    Object.entries(bankDisplayNames).map(([key, label]) => [label, key])
);

interface ComparisonTableProps {
    activeTerm: string;
}

interface MortgageRateComparison {
    bankName: string;
    listRate: number | null;
    avgRate: number | null;
    diff: number | null;
    lastChanged: string | null;
}

interface ComparisonResponse {
    averageMonth: string | null;
    averageMonthFormatted: string | null;
    rows: MortgageRateComparison[];
}

type SortDirection = "up" | "down";

const ComparisonTable: FC<ComparisonTableProps> = ({ activeTerm }) => {
    const { t } = useTranslation();

    const [data, setData] = useState<ComparisonResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [sortColumn, setSortColumn] = useState<string | null>(null);
    const [sortDirection, setSortDirection] = useState<SortDirection | null>(null);

    // Mobil: öppen rad
    const [openIndex, setOpenIndex] = useState<number | null>(null);

    function toggleMobileRow(index: number) {
        setOpenIndex(prev => (prev === index ? null : index));
    }

    // ============================================================
    // HÄMTA DATA
    // ============================================================
    useEffect(() => {
        async function fetchData() {
            setLoading(true);
            setError("");

            try {
                const json = await apiGet<ComparisonResponse>(
                    `/api/rates/comparison?term=${activeTerm}`
                );
                setData(json);
            } catch {
                setError(t("rates.comparison.error"));
            }

            setLoading(false);
        }

        fetchData();
    }, [activeTerm, t]);

    // ============================================================
    // SORTERING
    // ============================================================
    function onHeaderClick(column: string) {
        if (sortColumn !== column) {
            setSortColumn(column);
            setSortDirection("down");
            return;
        }

        setSortDirection(prev => (prev === "down" ? "up" : "down"));
    }

    function sortRows(rows: MortgageRateComparison[]) {
        if (!sortColumn || !sortDirection) return rows;

        const sorted = [...rows];

        sorted.sort((a, b) => {
            let A: number | string | null = null;
            let B: number | string | null = null;

            if (sortColumn === "bankName") {
                A = a.bankName;
                B = b.bankName;
                const cmp = A.localeCompare(B, "sv");
                return sortDirection === "down" ? cmp : -cmp;
            }

            if (sortColumn === "lastChanged") {
                A = a.lastChanged ? new Date(a.lastChanged).getTime() : null;
                B = b.lastChanged ? new Date(b.lastChanged).getTime() : null;
            }

            if (sortColumn === "listRate") {
                A = a.listRate;
                B = b.listRate;
            }

            if (sortColumn === "avgRate") {
                A = a.avgRate;
                B = b.avgRate;
            }

            if (sortColumn === "diff") {
                A = a.diff;
                B = b.diff;
            }

            if (A == null && B == null) return 0;
            if (A == null) return 1;
            if (B == null) return -1;

            return sortDirection === "down"
                ? (A as number) - (B as number)
                : (B as number) - (A as number);
        });

        return sorted;
    }

    function sortIcon(column: string) {
        if (sortColumn !== column || !sortDirection) {
            return <span className="text-icon-neutral">▷</span>;
        }
        return sortDirection === "down"
            ? <span className="text-primary">▼</span>
            : <span className="text-primary">▲</span>;
    }

    // ============================================================
    // RENDER
    // ============================================================
    if (loading) return <p>{t("rates.comparison.loading")}</p>;
    if (error) return <p className="text-negative">{error}</p>;
    if (!data) return <p>{t("rates.comparison.noData")}</p>;

    const { averageMonthFormatted, rows } = data;
    const sortedRows = sortRows(rows);

    const rowsWithRates = sortedRows.filter(r =>
        r.listRate != null || r.avgRate != null || r.lastChanged != null
    );

    const rowsWithoutRates = sortedRows.filter(r =>
        r.listRate == null && r.avgRate == null && r.lastChanged == null
    );

    return (
        <div className="flex flex-col gap-4">
            {/* ===================== */}
            {/* TABELL: BANKER MED RÄNTOR (OFÖRÄNDRAD) */}
            {/* ===================== */}
            <div className="overflow-x-auto border border-border rounded-lg bg-white">
                <table className="min-w-full">
                    <thead className="bg-bg-light text-text-primary">
                    <tr>
                        <th
                            className="pl-2 pr-0 sm:px-4 py-2 sm:py-3 text-left whitespace-nowrap text-sm sm:text-sm"
                            onClick={() => onHeaderClick("bankName")}
                        >
                            {t("rates.comparison.columns.bank")} {sortIcon("bankName")}
                        </th>

                        <th
                            className="px-0 pr-4 sm:px-4 py-2 sm:py-3 text-left cursor-pointer text-sm sm:text-sm"
                            onClick={() => onHeaderClick("listRate")}
                        >
                            <span className="inline-flex items-center gap-1 whitespace-nowrap">
                                {t("rates.comparison.columns.listRate")}
                                {sortIcon("listRate")}
                            </span>
                        </th>

                        <th
                            className="hidden md:table-cell px-4 py-3 text-left cursor-pointer select-none"
                            onClick={() => onHeaderClick("diff")}
                        >
                            {t("rates.comparison.columns.change")} {sortIcon("diff")}
                        </th>

                        <th
                            className="hidden md:table-cell px-4 py-3 text-left cursor-pointer select-none"
                            onClick={() => onHeaderClick("lastChanged")}
                        >
                            {t("rates.comparison.columns.lastChanged")} {sortIcon("lastChanged")}
                        </th>

                        <th
                            className="px-0 sm:px-4 py-2 sm:py-3 text-left cursor-pointer text-sm sm:text-sm leading-tight sm:leading-normal"
                            onClick={() => onHeaderClick("avgRate")}
                        >
                            <span className="hidden sm:inline">
                                {t("rates.comparison.columns.avgRate")}
                                {averageMonthFormatted ? ` (${averageMonthFormatted})` : ""}
                            </span>

                            <span className="sm:hidden">
                                {t("rates.comparison.columns.avgRate")}
                                <br />
                                {averageMonthFormatted ? `(${averageMonthFormatted})` : ""}
                            </span>

                            {sortIcon("avgRate")}
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    {rowsWithRates.map((row, index) => {
                        const isOpen = openIndex === index;
                        const diff = row.diff;

                        const rateClass =
                            diff == null
                                ? "bg-gray-100 text-gray-700"
                                : diff < 0
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700";

                        return (
                            <React.Fragment key={row.bankName}>
                                <tr className="hover:bg-row-hover">
                                    <td className="pl-2 sm:px-4 py-2 sm:py-3">
                                        <NavLink
                                            to={`/bank/${bankNameToKey[row.bankName]}`}
                                            className="
                                                text-primary hover:underline
                                                block
                                                max-w-[140px] sm:max-w-none
                                                sm:text-base
                                                overflow-hidden
                                                line-clamp-2
                                            "
                                        >
                                            {row.bankName}
                                        </NavLink>
                                    </td>

                                    <td
                                        className="pr-2 sm:px-4 py-3 cursor-pointer md:cursor-default"
                                        onClick={() => toggleMobileRow(index)}
                                    >
                                        {row.listRate != null ? (
                                            <div className="flex items-center gap-1">
                                                <span
                                                    className={`
                                                        inline-flex items-center gap-1
                                                        px-2 h-[26px] rounded-lg
                                                        text-xs sm:text-sm font-medium
                                                        ${rateClass}
                                                    `}
                                                >
                                                    {row.listRate.toFixed(2)}%
                                                    {diff != null && (diff < 0 ? " ▼" : " ▲")}
                                                </span>

                                                <span className="md:hidden text-gray-400 text-xl">›</span>
                                            </div>
                                        ) : (
                                            "–"
                                        )}
                                    </td>

                                    <td className="hidden md:table-cell px-4 py-3">
                                        {diff == null ? "–" : (
                                            <span
                                                className={`inline-flex items-center gap-1 px-2 h-[26px] rounded-lg text-xs font-medium ${
                                                    diff < 0
                                                        ? "bg-green-100 text-green-700"
                                                        : "bg-red-100 text-red-700"
                                                }`}
                                            >
                                                {diff < 0 ? "▼" : "▲"} {Math.abs(diff).toFixed(2)}%
                                            </span>
                                        )}
                                    </td>

                                    <td className="hidden md:table-cell px-4 py-3">
                                        {row.lastChanged ?? "–"}
                                    </td>

                                    <td className="px-4 py-3">
                                        {row.avgRate != null
                                            ? `${row.avgRate.toFixed(2)}%`
                                            : "–"}
                                    </td>
                                </tr>

                                {isOpen && (
                                    <tr className="md:hidden bg-gray-50">
                                        <td colSpan={3} className="px-4 py-2 text-xs text-text-secondary">
                                            <div>
                                                <span className="font-medium text-text-primary">
                                                    {t("rates.comparison.columns.change")}:
                                                </span>{" "}
                                                {diff == null
                                                    ? "–"
                                                    : `${diff < 0 ? "▼" : "▲"} ${Math.abs(diff).toFixed(2)}%`}
                                            </div>

                                            <div>
                                                <span className="font-medium text-text-primary">
                                                    {t("rates.comparison.columns.lastChanged")}:
                                                </span>{" "}
                                                {row.lastChanged ?? "–"}
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </React.Fragment>
                        );
                    })}
                    </tbody>
                </table>
            </div>

            {/* ===================== */}
            {/* BOX: BANKER UTAN RÄNTOR (LISTA – SOM DIN REFERENSBILD) */}
            {/* ===================== */}
            {rowsWithoutRates.length > 0 && (
                <div className="border border-border rounded-lg bg-white overflow-hidden">
                    <div className="bg-bg-light text-text-primary">
                        <div className="pl-2 sm:px-4 py-3 text-left font-semibold text-sm">
                            Banker utan räntor för vald bindningstid
                        </div>
                    </div>

                    <div>
                        {rowsWithoutRates.map((row) => (
                            <div
                                key={row.bankName}
                                className="hover:bg-row-hover"
                            >
                                <div className="pl-2 sm:px-4 py-3">
                                    <NavLink
                                        to={`/bank/${bankNameToKey[row.bankName]}`}
                                        className="text-primary hover:underline"
                                    >
                                        {row.bankName}
                                    </NavLink>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ComparisonTable;