import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

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

            {/* INTRO */}
            <Section>
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.intro.title")}
                    </h2>

                    {introParagraphs.map((text, i) => (
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
            <Section>
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.rates.title")}
                    </h2>

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
            <Section>
                <div className="max-w-4xl mx-auto p-2 sm:py-4 sm:px-6">
                    <h2 className="text-xl font-semibold mb-6">
                        {t("guide.comparison.title")}
                    </h2>

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
            <Section>
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
            <Section>
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
            <Section>
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