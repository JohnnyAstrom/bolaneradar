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
                bg-bg-light
                rounded-lg
                p-2
                mb-8
                md:p-10
            "
        >
            {/* Titel */}
            <h1
                className="
                    text-2xl
                    font-bold
                    text-text-primary
                    mb-3
                    text-center
                    md:text-left
                    md:text-3xl
                "
            >
                {t("home.hero.title")}
            </h1>

            {/* Beskrivning */}
            <p
                className="
                    text-text-secondary
                    mb-6
                    text-center
                    md:text-left
                    md:max-w-2xl
                "
            >
                {t("home.hero.description")}
            </p>

            {/* Knapp */}
            <div className="flex justify-center md:justify-start">
                <button
                    onClick={onToggleTest}
                    className={`
                        px-6 py-3 
                        rounded-lg 
                        font-medium 
                        transition-colors 
                        text-white
                        ${smartTestActive
                        ? "bg-primary-active hover:bg-primary-hover active:bg-primary"
                        : "bg-primary hover:bg-primary-hover active:bg-primary-active"
                    }
                    `}
                >
                    {smartTestActive
                        ? t("home.hero.ctaStop")
                        : t("home.hero.ctaStart")}
                </button>
            </div>
        </section>
    );
};

export default Hero;