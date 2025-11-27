/**
 * BankCurrentRatesTable.tsx
 *
 * Visar bindningstider + listränta + förändring + snittränta.
 * Listränta färgkodas exakt som i ComparisonTable:
 *
 *  - diff < 0 → grön badge, pil ner (▼)
 *  - diff > 0 → röd badge, pil upp (▲)
 */

import type { FC } from "react";
import type { BankRateRow } from "../../client/bankApi";

interface Props {
    rows: BankRateRow[];
    averageMonthFormatted: string | null;
}

// Dynamisk rubrik för snitträntan
const makeHeaders = (averageMonthFormatted: string | null) => [
    "Bindningstid",
    "Listränta",
    "Förändring",
    averageMonthFormatted
        ? `Snittränta (${averageMonthFormatted})`
        : "Snittränta",
    "Senast ändrad",
];

const BankCurrentRatesTable: FC<Props> = ({ rows, averageMonthFormatted }) => {
    return (
        <div>
            <h2 className="text-2xl font-semibold text-text-primary mb-5">
                Aktuella bolåneräntor
            </h2>

            <div className="overflow-x-auto border border-border rounded-lg bg-white">
                <table className="min-w-full">

                    {/* Kolumnrubriker */}
                    <thead className="bg-bg-light text-text-primary">
                    <tr>
                        {makeHeaders(averageMonthFormatted).map((h) => (
                            <th
                                key={h}
                                className="px-4 py-3 text-left"
                            >
                                {h}
                            </th>
                        ))}
                    </tr>
                    </thead>

                    {/* Tabellens rader */}
                    <tbody className="text-text-primary">

                    {rows.map((row, index) => {
                        const diff = row.change;

                        // Badge-färglogik exakt som ComparisonTable
                        const rateClass =
                            diff == null
                                ? "bg-gray-100 text-gray-700"
                                : diff < 0
                                    ? "bg-green-100 text-green-700"
                                    : "bg-red-100 text-red-700";

                        return (
                            <tr
                                key={index}
                                className="hover:bg-row-hover transition-colors"
                            >
                                {/* Bindningstid */}
                                <td className="px-4 py-3">{row.term}</td>

                                {/* Listränta – badge med färg & pil */}
                                <td className="px-4 py-3">
                                    {row.currentRate !== null ? (
                                        <span
                                            className={`
                                                inline-flex items-center gap-1
                                                px-2 h-[28px] rounded-xl
                                                text-sm font-medium ${rateClass}
                                            `}
                                        >
                                            {row.currentRate}%
                                        </span>
                                    ) : (
                                        "–"
                                    )}
                                </td>

                                {/* Förändring – badge med pil */}
                                <td className="px-4 py-3">
                                    {diff == null ? (
                                        "–"
                                    ) : (
                                        <span
                                            className={`
                                                inline-flex items-center gap-1
                                                px-2 h-[28px] rounded-xl text-sm font-medium
                                                ${diff < 0
                                                ? "bg-green-100 text-green-700"
                                                : "bg-red-100 text-red-700"}
                                            `}
                                        >
                                            {diff < 0 ? "▼" : "▲"}{" "}
                                            {Math.abs(diff).toFixed(2)}%
                                        </span>
                                    )}
                                </td>

                                {/* Snittränta */}
                                <td className="px-4 py-3">
                                    {row.avgRate !== null ? `${row.avgRate}%` : "–"}
                                </td>

                                {/* Senast ändrad */}
                                <td className="px-4 py-3">
                                    {row.lastChanged ?? "–"}
                                </td>
                            </tr>
                        );
                    })}

                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default BankCurrentRatesTable;