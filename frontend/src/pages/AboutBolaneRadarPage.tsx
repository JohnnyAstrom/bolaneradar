import { useTranslation } from "react-i18next";
import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";
import { BadgeInfo, Mail } from "lucide-react";

export default function AboutBolaneRadarPage() {
    const { t } = useTranslation();

    return (
        <PageWrapper>
            <section className="px-1 sm:px-6 mb-4 sm:mb-8">
                <div className="rounded-[28px] border border-border/60 bg-gradient-to-br from-slate-50 via-white to-sky-50 px-6 py-7 shadow-sm sm:px-8">
                    <div className="flex items-start gap-4">
                        <div className="hidden sm:inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                            <BadgeInfo size={22} />
                        </div>
                        <div className="max-w-3xl">
                            <h1 className="text-3xl font-bold tracking-tight text-text-primary mb-3">
                                {t("about.title")}
                            </h1>
                            <p className="text-text-secondary leading-7">
                                {t("about.intro")}
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            {/* OM BOLÅNERADAR */}
            <Section contentClassName="rounded-[28px]">
                <div
                    className="
                        max-w-4xl mx-auto
                        p-2
                        sm:py-4 sm:px-6
                    "
                >
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
                            <Mail size={18} />
                        </div>
                        <h2 className="text-xl font-semibold">
                            {t("about.contactTitle")}
                        </h2>
                    </div>

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
