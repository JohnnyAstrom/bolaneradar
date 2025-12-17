import type { FC } from "react";
import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
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
                setError("Kunde inte hämta räntedata just nu.");
            }

            setLoading(false);
        }

        fetchData();
    }, [activeTerm]);

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

            // =====================
            // BANKNAMN (string)
            // =====================
            if (sortColumn === "bankName") {
                const cmp = a.bankName.localeCompare(b.bankName, "sv");
                return sortDirection === "down" ? cmp : -cmp;
            }

            // =====================
            // SENAST ÄNDRAD (datum)
            // =====================
            if (sortColumn === "lastChanged") {
                const A = a.lastChanged ? new Date(a.lastChanged).getTime() : null;
                const B = b.lastChanged ? new Date(b.lastChanged).getTime() : null;

                if (A == null && B == null) return 0;
                if (A == null) return 1;
                if (B == null) return -1;

                return sortDirection === "down"
                    ? B - A   // nyast först
                    : A - B;
            }

            // =====================
            // NUMERISKA KOLUMNER
            // =====================
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

            return sortDirection === "down"
                ? A - B
                : B - A;
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
    if (loading) return <p>Hämtar aktuella räntor...</p>;
    if (error) return <p className="text-negative">{error}</p>;
    if (!data) return <p>Ingen data tillgänglig.</p>;

    const { averageMonthFormatted, rows } = data;
    const sortedRows = sortRows(rows);

    // 🔽 ENDA NYA LOGIKEN
    const rowsWithRates = sortedRows.filter(r =>
        r.listRate != null || r.avgRate != null || r.lastChanged != null
    );

    const rowsWithoutRates = sortedRows.filter(r =>
        r.listRate == null && r.avgRate == null && r.lastChanged == null
    );

    return (
        <div className="flex flex-col gap-4">

            {/* ===================== */}
            {/* TABELL: BANKER MED RÄNTOR */}
            {/* ===================== */}
            <div className="overflow-x-auto border border-border rounded-lg">
                <table className="min-w-full bg-white">

                    <thead className="bg-bg-light text-text-primary">
                    <tr>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("bankName")}>
                            Bank {sortIcon("bankName")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("listRate")}>
                            Listränta {sortIcon("listRate")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("diff")}>
                            Förändring {sortIcon("diff")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("avgRate")}>
                            Snittränta {averageMonthFormatted ? `(${averageMonthFormatted})` : ""} {sortIcon("avgRate")}
                        </th>
                        <th className="px-4 py-3 text-left cursor-pointer select-none" onClick={() => onHeaderClick("lastChanged")}>
                            Senast ändrad {sortIcon("lastChanged")}
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

            {/* ===================== */}
            {/* TABELL: BANKER UTAN RÄNTOR */}
            {/* ===================== */}
            {rowsWithoutRates.length > 0 && (
                <div className="overflow-x-auto border border-border rounded-lg">
                    <table className="min-w-full bg-white">

                        <thead className="bg-bg-light text-text-primary">
                        <tr>
                            <th className="px-4 py-3 text-left" colSpan={5}>
                                Banker utan räntor för vald bindningstid
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