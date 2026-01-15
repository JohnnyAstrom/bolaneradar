/**
 * useBankIntro.ts
 *
 * En liten React-hook som hämtar bankens introduktionsdata
 * beskrivning, och USP via getBankIntro().
 *
 * Syfte:
 *  - Hålla BankPage ren
 *  - Hantera loading/error på ett ställe
 */

import { useEffect, useState } from "react";
import {type BankIntro, getBankIntro} from "../services/bankApi";
import {useTranslation} from "react-i18next";

export function useBankIntro(bankKey: string) {
    const { i18n } = useTranslation();
    const language = i18n.language; // 👈 viktigt

    const [data, setData] = useState<BankIntro | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        setLoading(true);
        setError(null);

        const load = async () => {
            try {
                const result = await getBankIntro(bankKey);
                if (isMounted) setData(result);
            } catch {
                if (isMounted) setError("Kunde inte hämta bankens introduktion.");
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        load();
        return () => { isMounted = false; };
    }, [bankKey, language]);

    return { data, loading, error };
}