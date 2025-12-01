import { useEffect, useState } from "react";
import type { FC } from "react";

const TableFooter: FC = () => {
    const [latestScrape, setLatestScrape] = useState<string>("...");

    useEffect(() => {
        async function load() {
            try {
                const res = await fetch("/api/rates/updates/latest/global");
                const data = await res.json();

                if (data.latestScrape) {
                    // Formatera till svensk tid
                    const date = new Date(data.latestScrape).toLocaleString("sv-SE", {
                        year: "numeric",
                        month: "2-digit",
                        day: "2-digit",
                        hour: "2-digit",
                        minute: "2-digit",
                    });

                    setLatestScrape(date);
                }
            } catch (e) {
                console.error("Failed to load latest scrape timestamp", e);
            }
        }

        load();
    }, []);

    return (
        <div className="mt-4 text-xs text-text-secondary leading-relaxed">
            <p>Källa: Bankernas publika webbsidor</p>
            <p>
                Senast uppdaterad av BolåneRadar: {latestScrape}
            </p>
        </div>
    );
};

export default TableFooter;