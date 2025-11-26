import type { FC } from "react";

const tableHeaders = [
    "Bindningstid",
    "Listränta",
    "Förändring",
    "Snittränta",
    "Senast ändrad",
];

const dummyRates = [
    { term: "3 mån", list: "3.79%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "1 år", list: "3.34%", change: "+0.10%", changeClass: "text-negative", avg: "2.72%", date: "2025-01-25" },
    { term: "2 år", list: "3.39%", change: "+0.10%", changeClass: "text-negative", avg: "2.72%", date: "2025-01-30" },
    { term: "3 år", list: "3.49%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "4 år", list: "3.64%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "5 år", list: "3.79%", change: "+0.10%", changeClass: "text-negative", avg: "2.72%", date: "2025-01-20" },
    { term: "6 år", list: "3.99%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "7 år", list: "3.99%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "8 år", list: "3.99%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "9 år", list: "3.99%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
    { term: "10 år", list: "3.99%", change: "-0.10%", changeClass: "text-positive", avg: "2.72%", date: "2025-01-20" },
];

const BankCurrentRatesTable: FC = () => {
    return (
        <div className="mt-10">

            {/* Titel */}
            <h2 className="text-xl font-semibold text-text-primary mb-4">
                Aktuella bolåneräntor
            </h2>

            {/* Tabell */}
            <div className="overflow-x-auto border border-border rounded-lg bg-white">
                <table className="min-w-full">

                    {/* Header */}
                    <thead className="bg-bg-light text-text-secondary">
                    <tr>
                        {tableHeaders.map((header) => (
                            <th
                                key={header}
                                className="px-4 py-3 text-left font-medium"
                            >
                                {header}
                            </th>
                        ))}
                    </tr>
                    </thead>

                    {/* Body */}
                    <tbody className="text-text-primary">
                    {dummyRates.map((row, i) => (
                        <tr
                            key={i}
                            className="hover:bg-row-hover transition-colors"
                        >
                            <td className="px-4 py-3">{row.term}</td>
                            <td className="px-4 py-3">{row.list}</td>
                            <td className={`px-4 py-3 ${row.changeClass}`}>{row.change}</td>
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

export default BankCurrentRatesTable;