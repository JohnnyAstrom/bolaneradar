import type { FC } from "react";
import { NavLink } from "react-router-dom";
import { bankKeyMap } from "../../config/bankKeyMap";

interface ComparisonTableProps {
    activeTerm?: string; // not used yet
}

const tableHeaders = [
    "Bank",
    "Listränta",
    "Förändring",
    "Snittränta",
    "Senast ändrad",
];

const dummyBanks = [
    { bank: "Swedbank", list: "3.79%", diff: "+0.05", diffClass: "text-negative", avg: "3.14%", date: "2025-01-12" },
    { bank: "Nordea", list: "3.82%", diff: "-0.02", diffClass: "text-positive", avg: "3.18%", date: "2025-01-10" },
    { bank: "Handelsbanken", list: "3.75%", diff: "+0.01", diffClass: "text-negative", avg: "3.10%", date: "2025-01-14" },
    { bank: "SEB", list: "3.88%", diff: "+0.04", diffClass: "text-negative", avg: "3.20%", date: "2025-01-11" },
    { bank: "SBAB", list: "3.69%", diff: "-0.01", diffClass: "text-positive", avg: "3.12%", date: "2025-01-15" },
    { bank: "ICA banken", list: "3.72%", diff: "+0.03", diffClass: "text-negative", avg: "3.16%", date: "2025-01-12" },
    { bank: "Länsförsäkringar Bank", list: "3.66%", diff: "-0.02", diffClass: "text-positive", avg: "3.09%", date: "2025-01-08" },
    { bank: "Danske Bank", list: "3.85%", diff: "+0.06", diffClass: "text-negative", avg: "3.22%", date: "2025-01-13" },
    { bank: "SkandiaBanken", list: "3.77%", diff: "+0.01", diffClass: "text-negative", avg: "3.15%", date: "2025-01-09" },
    { bank: "Landshypotek Bank", list: "3.64%", diff: "-0.03", diffClass: "text-positive", avg: "3.05%", date: "2025-01-07" },
    { bank: "Ålandsbanken", list: "3.71%", diff: "0.00", diffClass: "text-text-secondary", avg: "3.13%", date: "2025-01-05" },
    { bank: "Ikano Bank", list: "4.20%", diff: "+0.10", diffClass: "text-negative", avg: "3.55%", date: "2025-01-12" },
];

const ComparisonTable: FC<ComparisonTableProps> = () => {
    return (
        <div className="flex flex-col gap-4">

            {/* Table */}
            <div className="overflow-x-auto border border-border rounded-lg">
                <table className="min-w-full bg-white">

                    <thead className="bg-bg-light text-text-secondary">
                    <tr>
                        {tableHeaders.map((header) => (
                            <th key={header} className="px-4 py-3 text-left font-medium">
                                <div className="flex items-center gap-1">
                                    {header}
                                    <span className="text-icon-neutral text-xs">▲</span>
                                </div>
                            </th>
                        ))}
                    </tr>
                    </thead>

                    <tbody className="text-text-primary">
                    {dummyBanks.map((row, i) => (
                        <tr key={i} className="hover:bg-row-hover transition-colors">
                            <td className="px-4 py-3">
                                <NavLink
                                    to={`/bank/${bankKeyMap[row.bank]}`}
                                    className="text-primary hover:underline"
                                >
                                    {row.bank}
                                </NavLink>
                            </td>
                            <td className="px-4 py-3">{row.list}</td>
                            <td className={`px-4 py-3 ${row.diffClass}`}>{row.diff}</td>
                            <td className="px-4 py-3">{row.avg}</td>
                            <td className="px-4 py-3">{row.date}</td>
                        </tr>
                    ))}
                    </tbody>

                </table>
            </div>

        </div>
    );
};

export default ComparisonTable;