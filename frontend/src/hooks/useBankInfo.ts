/**
 * useBankInfo.ts
 *
 * Hook som hämtar den fördjupade informationssidan för en bank.
 * Innehåller:
 *  - title (bankens huvudrubrik)
 *  - intro (kortare introduktionstext)
 *  - deepInsights (sektioner med heading + text)
 *  - faq (vanliga frågor)
 *  - CTA (länk + label)
 *
 * Syfte:
 *  - Förse BankInfoPage med färdig data
 *  - Hantera loading/error på ett ställe
 */

import { useEffect, useState, startTransition } from "react";
import { getBankInfo } from "../client/bankApi";

export interface BankInfo {
    intro: string;
    deepInsights: { heading: string; text: string }[];
    faq: { question: string; answer: string }[];
    ctaLabel: string;
    ctaUrl: string;
}

export function useBankInfo(bankKey: string | undefined) {
    const [info, setInfo] = useState<BankInfo | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        if (!bankKey) return;

        // Starta ny laddning utan att blockera UI
        startTransition(() => {
            setLoading(true);
            setError(false);
        });

        getBankInfo(bankKey)
            .then((data) => {
                setInfo(data);
            })
            .catch(() => {
                setError(true);
            })
            .finally(() => {
                setLoading(false);
            });

    }, [bankKey]);

    return { info, loading, error };
}