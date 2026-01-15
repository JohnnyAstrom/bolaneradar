import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

export default function AboutBolaneRadarPage() {
    const { t } = useTranslation();

    return (
        <PageWrapper>

            {/* OM BOLÅNERADAR */}
            <Section>
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-6">
                        {t("about.title")}
                    </h2>

                    <p className="text-text-secondary leading-relaxed mb-6">
                        {t("about.intro")}
                    </p>

                    <p className="text-text-secondary leading-relaxed mb-6">
                        {t("about.noAds")}
                    </p>

                    <p className="text-text-secondary leading-relaxed mb-6">
                        {t("about.smartRate")}
                    </p>

                    <p className="text-text-secondary leading-relaxed mb-6">
                        {t("about.architecture")}
                    </p>

                    <p className="text-text-secondary leading-relaxed">
                        {t("about.future")}
                    </p>
                </div>
            </Section>

            {/* KONTAKT & ANSVAR */}
            <Section>
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
                    <h2 className="text-xl font-semibold mb-6">
                        {t("about.contactTitle")}
                    </h2>

                    <p className="text-text-secondary leading-relaxed mb-4">
                        {t("about.contactIntro")}
                    </p>

                    <p className="text-text-secondary leading-relaxed">
                        <strong>{t("about.email")}:</strong>{" "}
                        <a
                            href="mailto:johnny.astrom@hotmail.com"
                            className="underline"
                        >
                            johnny.astrom@hotmail.com
                        </a>
                        <br />
                        <strong>LinkedIn:</strong>{" "}
                        <a
                            href="https://www.linkedin.com/in/johnny-astrom/"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="underline"
                        >
                            linkedin.com/in/johnny-astrom
                        </a>
                        <br />
                        <strong>GitHub:</strong>{" "}
                        <a
                            href="https://github.com/JohnnyAstrom/bolaneradar"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="underline"
                        >
                            github.com/JohnnyAstrom/bolaneradar
                        </a>
                    </p>
                </div>
            </Section>

        </PageWrapper>
    );
}
