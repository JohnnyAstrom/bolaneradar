import { useEffect, useState } from "react";
import type { FC } from "react";
import { useTranslation } from "react-i18next";
import { apiGet } from "../../services/client";

const TableFooter: FC = () => {
    const { t } = useTranslation();
    const [latestScrape, setLatestScrape] = useState<string>("...");

    useEffect(() => {
        async function load() {
            try {
                const data = await apiGet<{ latestScrape?: string }>(
                    "/api/rates/updates/latest/global"
                );

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
        <div className="mt-4 text-xs text-text-secondary leading-relaxed px-1">
            <p>{t("rates.tableFooter.source")}</p>
            <p>
                {t("rates.tableFooter.lastUpdated", {
                    date: latestScrape,
                })}
            </p>
        </div>
    );
};

export default TableFooter;