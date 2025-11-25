import type { FC } from "react";

const SmartRateTestResult: FC = () => {
    return (
        <div className="flex flex-col gap-8">

            {/* Titel + statusbadge */}
            <div>
                <h2 className="text-2xl font-bold text-text-primary mb-3">
                    Din räntestatus
                </h2>

                <span className="
          inline-block px-3 py-1 rounded-full
          bg-negative text-white text-sm font-medium
        ">
          Högre än snittet
        </span>
            </div>

            {/* Huvudtext */}
            <div className="flex flex-col gap-4 text-text-secondary leading-relaxed">

                <p>
                    Du har idag Swedbank som bank för ditt bolån.
                    Swedbank använder individuell räntesättning där engagemang, sparande och kundrelation påverkar rabatten.
                </p>

                <p>
                    Din ränta är <span className="text-negative font-semibold">0.32 procentenheter högre</span> än Swedbanks snittränta.
                </p>

                <p>
                    Jämfört med övriga banker ligger din ränta upp till
                    <span className="text-negative font-semibold"> 0.35 procentenheter högre</span>.
                </p>

                <p>
                    Din nuvarande ränta är baserad på <span className="font-semibold text-text-primary">bankens listränta vid din förra ränteändringsdag</span>.
                    Den speglar därför inte nödvändigtvis dagens ränteläge.
                </p>

                <p>
                    Vid nästa ränteändringsdag får du en ny ränta baserat på bankens aktuella listränta och din personliga rabatt.
                </p>

                <p>
                    Om du vet vilken rabatt du har kan du själv räkna ut vad din ungefärliga ränta blir efter nästa ränteändringsdag.
                    <span className="font-semibold text-text-primary">Ta bankens aktuella listränta och dra bort din personliga rabatt</span> – det ger en bra uppskattning av din kommande ränta.
                </p>
            </div>

            {/* Rekommendation */}
            <div>
                <h3 className="text-xl font-bold text-text-primary mb-3">
                    Rekommendation
                </h3>

                <p className="text-text-secondary leading-relaxed">
                    Eftersom din ränta ligger högre än både banken och marknaden kan det vara läge att kontakta banken för att se över din rabatt eller jämföra med andra banker.
                </p>
            </div>

        </div>
    );
};

export default SmartRateTestResult;