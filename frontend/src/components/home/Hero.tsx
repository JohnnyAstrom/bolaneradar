import type { FC } from "react";
import { useTranslation } from "react-i18next";

interface HeroProps {
    smartTestActive: boolean;
    onToggleTest: () => void;
}

const Hero: FC<HeroProps> = ({ smartTestActive, onToggleTest }) => {
    const { t } = useTranslation();

    return (
        <section
            className="
                relative overflow-hidden
                rounded-[28px]
                border border-border/60
                bg-gradient-to-br from-slate-50 via-white to-sky-50
                mx-1 sm:mx-6
                px-5 py-8
                mb-6
                shadow-sm
                md:px-8
            "
            style={{
                paddingTop: smartTestActive ? "1.75rem" : "2.5rem",
                paddingBottom: smartTestActive ? "2rem" : "2.5rem",
            }}
        >
            <div className="absolute inset-y-0 right-0 hidden md:block w-1/3 bg-[radial-gradient(circle_at_top_right,_rgba(59,130,246,0.12),_transparent_65%)]" />

            <div className="relative">
            <h1
                className="
                    text-3xl
                    font-bold
                    tracking-tight
                    text-text-primary
                    mb-3
                    text-left
                    md:text-4xl
                    md:max-w-3xl
                "
            >
                {t("home.hero.title")}
            </h1>

            <p
                className="
                    text-text-secondary
                    mb-7
                    text-base leading-8
                    text-left
                    md:max-w-2xl
                "
            >
                {t("home.hero.description")}
            </p>

            <div className="flex justify-start">
                <button
                    onClick={onToggleTest}
                    className={`
                        inline-flex items-center justify-center
                        min-w-[190px]
                        px-6 py-3.5
                        rounded-2xl
                        font-semibold
                        shadow-sm transition-all duration-200
                        text-white
                        ${smartTestActive
                        ? "bg-primary-active hover:bg-primary-hover active:bg-primary translate-y-0"
                        : "bg-primary hover:bg-primary-hover active:bg-primary-active hover:-translate-y-[1px]"
                    }
                    `}
                >
                    {smartTestActive
                        ? t("home.hero.ctaStop")
                        : t("home.hero.ctaStart")}
                </button>
            </div>
            </div>
        </section>
    );
};

export default Hero;
