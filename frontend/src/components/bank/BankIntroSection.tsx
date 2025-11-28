import type { FC } from "react";
import BankLogo from "./BankLogo";
import { bankDisplayNames } from "../../config/bankDisplayNames";
import { Check } from "lucide-react";

interface BankIntroSectionProps {
    bankKey: string;
    logoUrl: string;

    description?: string;
    uspItems?: string[];

    primaryCtaLabel?: string;
    secondaryCtaLabel?: string;

    primaryCtaUrl?: string;
    secondaryCtaUrl?: string;
}

const BankIntroSection: FC<BankIntroSectionProps> = ({
         bankKey,
         logoUrl,
         description = "Här kommer en kortfattad sammanfattning om banken. Detta ersätts senare av dynamisk text från backend.",
         uspItems = [
             "USP placeholder – ersätts med dynamisk bankdata.",
             "USP placeholder – t.ex. förmåner eller kundvillkor.",
             "USP placeholder – t.ex. rådgivning eller digitala tjänster."
         ],

         primaryCtaLabel = "Gå till bankens bolånesida",
         primaryCtaUrl = "#",

         secondaryCtaLabel,
         secondaryCtaUrl,
     }) => {

    const displayName = bankDisplayNames[bankKey] ?? bankKey;

    return (
        <div className="
            max-w-4xl mx-auto
            pt-0 pb-0 px-0     /* mobil */
            sm:pt-10 sm:pb-12 sm:px-4  /* desktop */
        ">

            {/* Logotyp */}
            <div>
                <BankLogo src={logoUrl} alt={displayName} bankKey={bankKey} />
            </div>

            {/* Beskrivning */}
            <p className="text-text-secondary max-w-2xl leading-relaxed mb-6">
                {description}
            </p>

            {/* USP-lista */}
            <ul className="space-y-3 mb-8">
                {uspItems.map((item, index) => (
                    <li key={index} className="flex items-start gap-2 text-text-primary">
                        <Check size={18} className="text-primary mt-1" />
                        <span>{item}</span>
                    </li>
                ))}
            </ul>

            {/* Knappar */}
            <div className="flex gap-4 mt-4 flex-wrap">
                {/* Primär CTA */}
                <a
                    href={primaryCtaUrl}
                    className="
                        px-6 py-3 bg-primary text-white rounded-lg text-sm font-medium
                        hover:bg-primary-hover active:bg-primary-active transition
                    "
                >
                    {primaryCtaLabel}
                </a>

                {/* Sekundär CTA – visas bara om den finns */}
                {secondaryCtaLabel && secondaryCtaUrl && (
                    <a
                        href={secondaryCtaUrl}
                        className="
                            px-6 py-3 border border-primary text-primary rounded-lg text-sm font-medium
                            hover:bg-primary/10 active:bg-primary/20 transition
                        "
                    >
                        {secondaryCtaLabel}
                    </a>
                )}
            </div>
        </div>
    );
};

export default BankIntroSection;