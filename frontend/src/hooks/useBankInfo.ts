/**
 * useBankInfo.ts
 *
 * Hook som hämtar den fördjupade informationssidan för en bank.
 */

import { useEffect, useState, startTransition } from "react";
import { useTranslation } from "react-i18next";
import { getBankInfo } from "../client/bankApi";
import type { BankInfo } from "../client/bankApi";

export function useBankInfo(bankKey: string | undefined) {
    const { i18n } = useTranslation();

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

    }, [bankKey, i18n.language]);

    return { info, loading, error };
}