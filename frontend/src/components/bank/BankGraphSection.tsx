import type { FC } from "react";
import { useEffect, useState } from "react";

import {
    fetchAvailableTerms,
    fetchHistoricalRates,
    type HistoricalPoint
} from "../../client/bankApi";

import { termLabelMap } from "../../config/termLabelMap";

import {
    ResponsiveContainer,
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
    Area
} from "recharts";

interface Props {
    bankName: string;
}

const BankGraphSection: FC<Props> = ({ bankName }) => {

    const [terms, setTerms] = useState<string[]>([]);
    const [selectedTerm, setSelectedTerm] = useState("");
    const [data, setData] = useState<{ effectiveDate: string; ratePercent: number }[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    /* ---------------------------------------------------------
     * Hjälpfunktion: formatMonth("2025-03-01") → "Mars 2025"
     * --------------------------------------------------------- */
    const formatMonth = (isoDate: string) => {
        const [year, month] = isoDate.split("-");
        const date = new Date(Number(year), Number(month) - 1);

        const monthName = date.toLocaleDateString("sv-SE", {
            month: "short"
        });

        const clean = monthName.replace(".", "");
        return `${clean.charAt(0).toUpperCase() + clean.slice(1)} ${year}`;
    };

    /* ---------------------------------------
     * 1) Hämta vilka bindningstider som är tillgängliga
     * --------------------------------------- */
    useEffect(() => {
        async function loadTerms() {
            try {
                const list = await fetchAvailableTerms(bankName);
                setTerms(list);

                if (list.includes("3M")) {
                    setSelectedTerm("3M");
                } else if (list.length > 0) {
                    setSelectedTerm(list[0]);
                }

            } catch {
                setTerms([]);
            }
        }
        loadTerms();
    }, [bankName]);

    /* ---------------------------------------
     * 2) Hämta historisk snittränta
     * --------------------------------------- */
    useEffect(() => {
        if (!selectedTerm) return;

        let isCancelled = false;

        async function loadHistory() {
            setLoading(true);
            setError(null);

            try {
                const rawPoints: HistoricalPoint[] = await fetchHistoricalRates(
                    bankName,
                    selectedTerm
                );

                if (isCancelled) return;

                const mapped = rawPoints.map((p) => ({
                    effectiveDate: p.month,
                    ratePercent: p.avgRate
                }));

                const sorted = mapped.sort((a, b) =>
                    a.effectiveDate.localeCompare(b.effectiveDate)
                );

                setData(sorted);
            } catch {
                if (!isCancelled) setError("Kunde inte hämta historik");
            } finally {
                if (!isCancelled) setLoading(false);
            }
        }

        loadHistory();
        return () => { isCancelled = true; };

    }, [selectedTerm, bankName]);

    /* ---------------------------------------
     * 3) Render
     * --------------------------------------- */
    return (
        <div>
            <h2 className="text-2xl font-semibold text-text-primary mb-2">
                Historisk utveckling
            </h2>

            <p className="text-text-secondary text-sm mb-6">
                Visar bankens genomsnittliga boränta de senaste 12 månaderna.
                <br />
                Välj bindningstid i dropdown menyn nedan:
            </p>

            <div className="flex mb-6">
                <select
                    value={selectedTerm}
                    onChange={(e) => setSelectedTerm(e.target.value)}
                    className="
                        px-4 py-2 border border-border rounded-md bg-white
                        text-text-primary text-sm
                        hover:border-icon-neutral focus:outline-none focus:ring-2 focus:ring-primary-light
                    "
                >
                    <option value="">Välj bindningstid</option>

                    {terms.map((t) => (
                        <option key={t} value={t}>
                            {termLabelMap[t]}
                        </option>
                    ))}
                </select>
            </div>

            {/* Grafcontainer – responsiv höjd & full bredd */}
            <div
                className="
                    w-full
                    bg-white
                    border border-border
                    rounded-lg
                    px-2 sm:px-4
                    py-4
                    text-text-secondary
                    text-xs sm:text-sm
                    h-[400px] sm:h-[450px] md:h-[500px]
                    min-h-[300px]
                "
            >
                {/* 1) Ingen term vald */}
                {!selectedTerm && (
                    <div className="w-full h-full flex items-center justify-center">
                        <span>Välj bindningstid för att visa graf</span>
                    </div>
                )}

                {/* 2) Laddar */}
                {selectedTerm && loading && (
                    <div className="w-full h-full flex items-center justify-center">
                        <span>Laddar...</span>
                    </div>
                )}

                {/* 3) Fel */}
                {selectedTerm && !loading && error && (
                    <div className="w-full h-full flex items-center justify-center">
                        <span className="text-red-600">{error}</span>
                    </div>
                )}

                {/* 4) Graf */}
                {selectedTerm && !loading && !error && data.length > 0 && (
                    <div className="w-full h-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={data}>
                                <defs>
                                    <linearGradient id="rateGradient" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#2563eb" stopOpacity={0.3} />
                                        <stop offset="100%" stopColor="#2563eb" stopOpacity={0.05} />
                                    </linearGradient>
                                </defs>

                                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />

                                <XAxis
                                    dataKey="effectiveDate"
                                    tickFormatter={(d) => formatMonth(d)}
                                    tick={{ fontSize: 10 }}
                                    stroke="#6B7280"
                                    interval="preserveStartEnd"
                                />

                                <YAxis
                                    width={window.innerWidth < 640 ? 32 : 50}
                                    domain={[1.0, 5.0]}
                                    ticks={[1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0]}
                                    tickFormatter={(v) =>
                                        window.innerWidth < 640
                                            ? `${v.toFixed(1)}%`
                                            : `${v.toFixed(2)} %`
                                    }
                                    tick={{ fontSize: 11 }}
                                    stroke="#6B7280"
                                />

                                <Tooltip
                                    formatter={(v: number) => [`${v.toFixed(2)} %`, "Snittränta"]}
                                    labelFormatter={(label: string) =>
                                        `Datum: ${formatMonth(label)}`
                                    }
                                    contentStyle={{
                                        borderRadius: "8px",
                                        background: "white",
                                        border: "1px solid #E5E7EB",
                                        fontSize: "12px"
                                    }}
                                />

                                <Area
                                    type="monotone"
                                    dataKey="ratePercent"
                                    stroke="none"
                                    fill="url(#rateGradient)"
                                    tooltipType="none"
                                />

                                <Line
                                    type="monotone"
                                    dataKey="ratePercent"
                                    name="Snittränta"
                                    stroke="#2563eb"
                                    strokeWidth={2}
                                    dot={{ r: 3 }}
                                    activeDot={{ r: 5 }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                )}

                {/* 5) Ingen data */}
                {selectedTerm && !loading && !error && data.length === 0 && (
                    <div className="w-full h-full flex items-center justify-center">
                        <span>Ingen historik finns för denna bindningstid.</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default BankGraphSection;