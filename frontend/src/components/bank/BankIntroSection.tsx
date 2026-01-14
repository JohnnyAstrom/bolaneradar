import type { FC } from "react";
import { useTranslation } from "react-i18next";
import { Check } from "lucide-react";

interface BankIntroSectionProps {
    description?: string;
    uspItems?: string[];
}

const BankIntroSection: FC<BankIntroSectionProps> = ({
                                                         description,
                                                         uspItems
                                                     }) => {
    const { t } = useTranslation();

    const fallbackDescription = t("bank.intro.fallbackDescription");

    const fallbackUsps = [
        t("bank.intro.fallbackUsp1"),
        t("bank.intro.fallbackUsp2"),
        t("bank.intro.fallbackUsp3"),
    ];

    return (
        <div
            className="
                max-w-4xl mx-auto
                pt-0 pb-0 px-0
                sm:py-4 sm:px-6
            "
        >
            <p className="text-text-secondary max-w-2xl leading-relaxed mb-6">
                {description ?? fallbackDescription}
            </p>

            <ul className="space-y-3 mb-8">
                {(uspItems && uspItems.length > 0 ? uspItems : fallbackUsps).map(
                    (item, index) => (
                        <li
                            key={index}
                            className="flex items-start gap-2 text-text-primary"
                        >
                            <Check size={18} className="text-primary mt-1" />
                            <span>{item}</span>
                        </li>
                    )
                )}
            </ul>
        </div>
    );
};

export default BankIntroSection;