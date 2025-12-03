import type { FC } from "react";
import type { SmartRateTestResult } from "../../types/smartRate";

interface Props {
    result: SmartRateTestResult | null | undefined;
}

const SmartRateTestResultView: FC<Props> = ({ result }) => {

    // Guard: om resultat saknas → rendera inget
    if (!result) return null;

    return (
        <div className="flex flex-col gap-8">

            {/* Titel + statusbadge */}
            <div>
                <h2 className="text-2xl font-bold text-text-primary mb-3">
                    Din räntestatus
                </h2>

                <span
                    className="
                        inline-block px-3 py-1 rounded-full
                        bg-negative text-white text-sm font-medium
                    "
                >
                    {result.status}
                </span>
            </div>

            {/* Huvudtext */}
            <div className="flex flex-col gap-4 text-text-secondary leading-relaxed">

                <p>
                    Du har idag <span className="font-semibold">{result.bank}</span> som bank.
                </p>

                <p>
                    Din ränta är
                    <span className="text-negative font-semibold">
                        {" "}{result.differenceFromBankAverage} procentenheter
                    </span>
                    {" "}
                    högre än bankens snittränta.
                </p>

                <p>
                    Jämfört med övriga banker ligger du upp till
                    <span className="text-negative font-semibold">
                        {" "}{result.differenceFromBestMarketAverage} procentenheter
                    </span>
                    högre.
                </p>

                <p>{result.analysisText}</p>
                <p>{result.additionalContext}</p>
            </div>

            {/* Rekommendation */}
            <div>
                <h3 className="text-xl font-bold text-text-primary mb-3">
                    Rekommendation
                </h3>
                <p className="text-text-secondary leading-relaxed">
                    {result.recommendation}
                </p>
            </div>

        </div>
    );
};

export default SmartRateTestResultView;