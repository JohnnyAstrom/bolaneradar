import type { FC } from "react";

const RatesHeader: FC = () => {
    return (
        <div className="mb-6">
            <h2 className="text-2xl font-bold text-text-primary mb-2">
                Aktuella bolåneräntor
            </h2>

            <p className="text-text-secondary leading-relaxed text-sm max-w-2xl">
                Tabellen nedan visar aktuella bolåneräntor från Sveriges största banker.
                Du kan välja bindningstider för att jämföra listräntor, snitträntor och se när
                respektive bank senast ändrade sina räntor. Klickar du på bankernas namn så
                kan du se historik vad gäller bankens snittränta.
            </p>
        </div>
    );
};

export default RatesHeader;