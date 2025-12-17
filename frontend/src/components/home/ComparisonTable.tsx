import type { FC } from "react";
import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { bankDisplayNames } from "../../config/bankDisplayNames";

// ============================================================
//  bankName → urlKey (inverterar displaynames)
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

    // ============================================================
    // HÄMTA DATA FRÅN API
    // ============================================================
    useEffect(() => {
        async function fetchData() {
            setLoading(true);
            setError("");

            try {
                const res = await fetch(`/api/rates/comparison?term=${activeTerm}`);
                if (!res.ok) throw new Error("Serverfel");

                const json = await res.json();
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
            if (sortColumn === "bankName") {
                const cmp = a.bankName.localeCompare(b.bankName, "sv");
                return sortDirection === "down" ? cmp : -cmp;
            }

            if (sortColumn === "lastChanged") {
                const A = a.lastChanged ? new Date(a.lastChanged).getTime() : null;
                const B = b.lastChanged ? new Date(b.lastChanged).getTime() : null;

                if (A == null && B == null) return 0;
                if (A == null) return 1;
                if (B == null) return -1;

                return sortDirection === "down" ? B - A : A - B;
            }

            let A: number | null = null;
            let B: number | null = null;

            if (sortColumn === "listRate") {
                A = a.listRate;
                B = b.listRate;
            } else if (sortColumn === "avgRate") {
                A = a.avgRate;
                B = b.avgRate;
            } else if (sortColumn === "diff") {
                A = a.diff;
                B = b.diff;
            }

            if (A == null && B == null) return 0;
            if (A == null) return 1;
            if (B == null) return -1;

            return sortDirection === "down" ? A - B : B - A;
        });

        return sorted;
    }

    // ============================================================
    // SORT-IKON
    // ============================================================
    function sortIcon(column: string) {
        if (sortColumn !== column || !sortDirection) {
            return <span className="text-icon-neutral">▷</span>;
        }
        return sortDirection === "down"
            ? <span className="text-primary">▼</span>
            : <span className="text-primary">▲</span>;
    }

    // ============================================================
    // RENDERING
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

            {/* TABELL: BANKER MED RÄNTOR */}
            <div className="overflow-x-auto border border-border rounded-lg">
                <table className="min-w-full bg-white">
                    <thead className="bg-bg-light text-text-primary">
                    <tr>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("bankName")}>
                            {t("rates.comparison.columns.bank")} {sortIcon("bankName")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("listRate")}>
                            {t("rates.comparison.columns.listRate")} {sortIcon("listRate")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("diff")}>
                            {t("rates.comparison.columns.change")} {sortIcon("diff")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("avgRate")}>
                            {t("rates.comparison.columns.avgRate")} {averageMonthFormatted ? `(${averageMonthFormatted})` : ""} {sortIcon("avgRate")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("lastChanged")}>
                            {t("rates.comparison.columns.lastChanged")} {sortIcon("lastChanged")}
                        </th>
                    </tr>
                    </thead>

                    <tbody>
                    {rowsWithRates.map((row) => (
                        <tr key={row.bankName} className="hover:bg-row-hover">
                            <td className="px-4 py-3">
                                <NavLink to={`/bank/${bankNameToKey[row.bankName]}`} className="text-primary hover:underline">
                                    {row.bankName}
                                </NavLink>
                            </td>

                            <td className="px-4 py-3">
                                {row.listRate != null ? (
                                    <span
                                        className={`
                                                inline-flex items-center justify-center
                                                h-[28px]
                                                px-2 rounded-xl text-sm font-medium mx-auto
                                                ${
                                            row.diff == null
                                                ? "bg-gray-100 text-gray-700"
                                                : row.diff < 0
                                                    ? "bg-green-100 text-green-700"
                                                    : "bg-red-100 text-red-700"
                                        }
                                            `}
                                    >
                                            {row.listRate.toFixed(2)}%
                                        </span>
                                ) : "–"}
                            </td>

                            <td className="px-4 py-3">
                                {row.diff == null ? "–" : (
                                    <span
                                        className={`
                                                inline-flex items-center justify-center gap-1
                                                h-[28px]
                                                px-2 rounded-xl text-sm font-medium mx-auto
                                                ${
                                            row.diff > 0
                                                ? "bg-red-100 text-red-700"
                                                : "bg-green-100 text-green-700"
                                        }
                                            `}
                                    >
                                            {row.diff > 0 ? "▲" : "▼"} {Math.abs(row.diff).toFixed(2)}%
                                        </span>
                                )}
                            </td>

                            <td className="px-4 py-3">
                                {row.avgRate != null ? `${row.avgRate.toFixed(2)}%` : "–"}
                            </td>

                            <td className="px-4 py-3">
                                {row.lastChanged ?? "–"}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* TABELL: BANKER UTAN RÄNTOR */}
            {rowsWithoutRates.length > 0 && (
                <div className="overflow-x-auto border border-border rounded-lg">
                    <table className="min-w-full bg-white">
                        <thead className="bg-bg-light text-text-primary">
                        <tr>
                            <th className="px-4 py-3 text-left" colSpan={5}>
                                {t("rates.comparison.noRatesTitle")}
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        {rowsWithoutRates.map((row) => (
                            <tr key={row.bankName} className="hover:bg-row-hover">
                                <td className="px-4 py-3">
                                    <NavLink to={`/bank/${bankNameToKey[row.bankName]}`} className="text-primary hover:underline">
                                        {row.bankName}
                                    </NavLink>
                                </td>
                                <td className="px-4 py-3">–</td>
                                <td className="px-4 py-3">–</td>
                                <td className="px-4 py-3">–</td>
                                <td className="px-4 py-3">–</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default ComparisonTable;