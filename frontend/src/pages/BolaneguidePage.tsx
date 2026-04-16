import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";
import { BookOpen, CircleHelp, Scale, TrendingUp } from "lucide-react";

function getArray(value: unknown): string[] {
    return Array.isArray(value) ? value : [];
}

export default function BolaneguidePage() {
    const { t } = useTranslation();

    const introParagraphs = getArray(
        t("guide.intro.paragraphs", { returnObjects: true })
    );

    const ratesParagraphs = getArray(
        t("guide.rates.paragraphs", { returnObjects: true })
    );

    const comparisonParagraphs = getArray(
        t("guide.comparison.paragraphs", { returnObjects: true })
    );

    const comparisonPoints = getArray(
        t("guide.comparison.points", { returnObjects: true })
    );

    const historyParagraphs = getArray(
        t("guide.history.paragraphs", { returnObjects: true })
    );

    const smartRateParagraphs = getArray(
        t("guide.smartRate.paragraphs", { returnObjects: true })
    );

    const smartRatePoints = getArray(
        t("guide.smartRate.points", { returnObjects: true })
    );

    const disclaimerParagraphs = getArray(
        t("guide.disclaimer.paragraphs", { returnObjects: true })
    );

    const disclaimerPoints = getArray(
        t("guide.disclaimer.points", { returnObjects: true })
    );

    return (
        <PageWrapper>
            <section className="px-1 sm:px-6 mb-4 sm:mb-8">
                <div className="rounded-[28px] border border-border/60 bg-gradient-to-br from-slate-50 via-white to-sky-50 px-6 py-7 shadow-sm sm:px-8">
                    <div className="flex items-start gap-4">
                        <div className="hidden sm:inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                            <BookOpen size={22} />
                        </div>
                        <div className="max-w-3xl">
                            <h1 className="text-3xl font-bold tracking-tight text-text-primary mb-3">
                                {t("guide.intro.title")}
                            </h1>
                            {introParagraphs.slice(0, 2).map((text, i) => (
                                <p
                                    key={i}
                                    className="text-text-secondary leading-7 mb-2 last:mb-0"
                                >
                                    {text}
                                </p>
                            ))}
                        </div>
                    </div>
                </div>
            </section>

            {/* INTRO */}
            <Section contentClassName="rounded-[28px]">
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <div className="flex items-center gap-3 mb-6">
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-primary">
                            <CircleHelp size={18} />
                        </div>
                        <h2 className="text-xl font-semibold">
                            {t("guide.comparison.title")}
                        </h2>
                    </div>

                    {introParagraphs.slice(2).map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}
                </div>
            </Section>

            {/* RÄNTOR */}
            <Section contentClassName="rounded-[28px]">
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-primary">
                            <TrendingUp size={18} />
                        </div>
                        <h2 className="text-xl font-semibold">
                        {t("guide.rates.title")}
                        </h2>
                    </div>

                    {ratesParagraphs.map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}
                </div>
            </Section>

            {/* JÄMFÖRELSER */}
            <Section contentClassName="rounded-[28px]">
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-primary">
                            <Scale size={18} />
                        </div>
                        <h2 className="text-xl font-semibold">
                        {t("guide.comparison.title")}
                        </h2>
                    </div>

                    {comparisonParagraphs.map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}

                    <ul className="list-disc pl-5 text-text-secondary space-y-2">
                        {comparisonPoints.map((point, i) => (
                            <li key={i}>{point}</li>
                        ))}
                    </ul>
                </div>
            </Section>

            {/* HISTORIK */}
            <Section contentClassName="rounded-[28px]">
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.history.title")}
                    </h2>

                    {historyParagraphs.map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}
                </div>
            </Section>

            {/* SMART RÄNTE-TEST */}
            <Section contentClassName="rounded-[28px]">
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.smartRate.title")}
                    </h2>

                    {smartRateParagraphs.map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}

                    <ul className="list-disc pl-5 text-text-secondary space-y-2 mb-4">
                        {smartRatePoints.map((point, i) => (
                            <li key={i}>{point}</li>
                        ))}
                    </ul>

                    <p className="text-text-secondary leading-relaxed">
                        {t("guide.smartRate.note")}
                    </p>
                </div>
            </Section>

            {/* DISCLAIMER */}
            <Section contentClassName="rounded-[28px]">
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.disclaimer.title")}
                    </h2>

                    {disclaimerParagraphs.map((text, i) => (
                        <p
                            key={i}
                            className="text-text-secondary leading-relaxed mb-4"
                        >
                            {text}
                        </p>
                    ))}

                    <ul className="list-disc pl-5 text-text-secondary space-y-2 mb-4">
                        {disclaimerPoints.map((point, i) => (
                            <li key={i}>{point}</li>
                        ))}
                    </ul>

                    <p className="text-text-secondary leading-relaxed">
                        {t("guide.disclaimer.closing")}
                    </p>
                </div>
            </Section>

        </PageWrapper>
    );
}
