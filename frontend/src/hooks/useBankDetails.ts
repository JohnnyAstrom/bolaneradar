/**
 * useBankDetails.ts
 *
 * Hook som hämtar bankens detaljerade information:
 *  - description (huvudtext)
 *  - overviewText (kort översikt)
 *  - bestFor / notFor-listor
 *  - CTA-länkar
 *
 * Syfte:
 *  - Hålla BankDetailsPage ren
 *  - Samla loading/error-hantering på ett ställe
 */

import { useEffect, useState } from "react";
import { getBankDetails } from "../client/bankApi";

export interface BankDetailsData {
    description: string;
    overviewText: string;
    bestFor: string[];
    notFor: string[];
    primaryCtaLabel?: string;
    primaryCtaUrl?: string;
    secondaryCtaLabel?: string;
    secondaryCtaUrl?: string;
}

export function useBankDetails(bankKey: string) {
    const [details, setDetails] = useState<BankDetailsData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        const load = async () => {
            try {
                const result = await getBankDetails(bankKey);
                if (isMounted) setDetails(result);
            } catch {
                setError("Kunde inte ladda bankinformation.");
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();
        return () => {
            isMounted = false;
        };
    }, [bankKey]);

    return { details, loading, error };
}