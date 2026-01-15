import type { FC } from "react";
import { useTranslation } from "react-i18next";

const RatesHeader: FC = () => {
    const { t } = useTranslation();

    return (
        <div className="mb-6">
            <h2 className="text-2xl font-bold text-text-primary mb-2 px-1">
                {t("rates.header.title")}
            </h2>

            <p className="text-text-secondary leading-relaxed text-sm max-w-2xl px-1">
                {t("rates.header.description")}
            </p>
        </div>
    );
};

export default RatesHeader;