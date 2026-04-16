import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";
import { CalendarClock } from "lucide-react";
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
            <section className="px-1 sm:px-6 mb-4 sm:mb-8">
                <div className="rounded-[28px] border border-border/60 bg-gradient-to-br from-slate-50 via-white to-sky-50 px-6 py-7 shadow-sm sm:px-8">
                    <div className="flex items-start gap-4">
                        <div className="hidden sm:inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                            <CalendarClock size={22} />
                        </div>
                        <div className="max-w-3xl">
                            <h1 className="text-3xl font-bold tracking-tight text-text-primary mb-3">
                                {t("rateUpdates.title")}
                            </h1>
                            <p className="text-text-secondary leading-7">
                                {t("rateUpdates.description")}
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            <Section contentClassName="rounded-[28px]">
                <div className="max-w-5xl mx-auto p-2 sm:py-4 sm:px-6 lg:px-10">
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
