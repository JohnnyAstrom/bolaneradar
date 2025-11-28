import type { FC } from "react";

interface HeroProps {
    smartTestActive: boolean;
    onToggleTest: () => void;
}

const Hero: FC<HeroProps> = ({ smartTestActive, onToggleTest }) => {
    return (
        <section
            className="
                bg-bg-light
                rounded-lg
                p-2
                mb-8
                md:p-10     /* större padding på desktop */
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
                Jämför bolåneräntor – snabbt & enkelt
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
                Få en överblick över bankernas bolåneräntor, se historiska trender
                och testa hur din ränta står sig mot snittet.
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
                    {smartTestActive ? "Avsluta räntetest" : "Starta räntetest"}
                </button>
            </div>

        </section>
    );
};

export default Hero;