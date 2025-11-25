import type { FC } from "react";

interface HeroProps {
    smartTestActive: boolean;
    onToggleTest: () => void;
}

const Hero: FC<HeroProps> = ({ smartTestActive, onToggleTest }) => {
    return (
        <section className="bg-bg-light rounded-lg p-10 mb-8">
            <h1 className="text-3xl font-bold text-text-primary mb-4">
                Jämför bolåneräntor – snabbt & enkelt
            </h1>

            <p className="text-text-secondary max-w-2xl mb-6">
                Få en överblick över bankernas bolåneräntor, se historiska trender
                och testa hur din ränta står sig mot snittet.
            </p>

            <button
                onClick={onToggleTest}
                className={`
                px-6 py-3 rounded-lg font-medium transition-colors text-white
                ${smartTestActive
                ? "bg-primary-active hover:bg-primary-hover active:bg-primary"
                : "bg-primary hover:bg-primary-hover active:bg-primary-active"
                }
                `}
            >
                {smartTestActive ? "Avsluta räntetest" : "Starta räntetest"}
            </button>

        </section>
    );
};

export default Hero;