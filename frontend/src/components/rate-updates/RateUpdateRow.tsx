import { useTranslation } from "react-i18next";
import type { RateUpdate } from "../../types/rateUpdates";

export function RateUpdateRow({ update }: { update: RateUpdate }) {
    const { t } = useTranslation();

    const diff = update.newRate - update.previousRate;
    const isIncrease = diff > 0;
    const isDecrease = diff < 0;

    return (
        <li
            className="
            grid
            grid-cols-[minmax(0,0.95fr)_0.95fr_0.95fr_0.95fr]
            w-full
            gap-x-2
            sm:grid-cols-[minmax(0,1fr)_88px_88px_88px]
            lg:grid-cols-[minmax(0,1fr)_96px_96px_96px]
            "
        >
            {/* Bindningstid */}
            <span className="min-w-0 text-gray-600">
                {t(`mortgage.termShort.${update.bindingPeriod}`)}
            </span>

            {/* Tidigare ränta */}
            <span className="text-gray-500 tabular-nums text-center">
                {update.previousRate.toFixed(2)}%
            </span>

            {/* Förändring */}
            <span
                className={`
                    tabular-nums text-center
                    ${
                    isIncrease
                        ? "text-red-500"
                        : isDecrease
                            ? "text-green-600"
                            : "text-gray-400"
                }
                `}
            >
                {isIncrease && "+"}
                {diff.toFixed(2)}%
            </span>

            {/* Ny ränta */}
            <span className="font-medium text-gray-900 tabular-nums text-center">
                {update.newRate.toFixed(2)}%
            </span>
        </li>
    );
}
