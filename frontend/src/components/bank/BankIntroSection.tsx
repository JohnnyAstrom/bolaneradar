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
        <div className="max-w-2xl">
            <div>
                <p className="max-w-xl text-base leading-7 text-text-secondary sm:text-[1.02rem]">
                    {description ?? fallbackDescription}
                </p>
            </div>

            <div className="mt-5 rounded-[22px] border border-slate-200 bg-slate-50/80 p-4 sm:p-5">
                <ul className="space-y-3">
                    {(uspItems && uspItems.length > 0 ? uspItems : fallbackUsps).map(
                        (item, index) => (
                            <li
                                key={index}
                                className="flex items-start gap-3 text-text-primary"
                            >
                                <span className="mt-0.5 inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white text-primary shadow-sm">
                                    <Check size={14} />
                                </span>
                                <span className="leading-7 text-[15px]">{item}</span>
                            </li>
                        )
                    )}
                </ul>
            </div>
        </div>
    );
};

export default BankIntroSection;
