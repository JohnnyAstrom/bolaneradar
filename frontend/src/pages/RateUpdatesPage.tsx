import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";
import { fetchRateUpdates } from "../services/rateUpdatesApi";
import type { RateUpdateDay } from "../types/rateUpdates";
import { RateUpdatesList } from "../components/rate-updates/RateUpdatesList";

export default function RateUpdatesPage() {
    const { t } = useTranslation();
    const [days, setDays] = useState<RateUpdateDay[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchRateUpdates()
            .then(data => {
                setDays(data);
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    return (
        <PageWrapper>
            <Section>
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    {/* Sidtitel */}
                    <h1 className="text-xl font-semibold mb-6">
                        {t("rateUpdates.title")}
                    </h1>

                    <p className="text-sm text-text-secondary mb-6">
                        {t("rateUpdates.description")}
                    </p>

                    {/* Innehåll */}
                    {loading ? (
                        <p className="text-text-secondary">
                            {t("rateUpdates.loading")}
                        </p>
                    ) : days.length === 0 ? (
                        <p className="text-text-secondary">
                            {t("rateUpdates.empty")}
                        </p>
                    ) : (
                        <RateUpdatesList days={days} />
                    )}
                </div>
            </Section>
        </PageWrapper>
    );
}