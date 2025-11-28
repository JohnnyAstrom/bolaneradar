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
import {type BankIntro, getBankIntro} from "../client/bankApi";

export function useBankIntro(bankKey: string) {
    // Här lagras själva datan från API:t.
    const [data, setData] = useState<BankIntro | null>(null);

    // Visar om API-anropet fortfarande laddar.
    const [loading, setLoading] = useState(true);

    // Visar om något gick fel (t.ex. felaktig bankKey).
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Flagga för att undvika state updates efter unmount.
        let isMounted = true;

        // Starta laddning varje gång bankKey ändras
        setLoading(true);
        setError(null);

        const load = async () => {
            try {
                const result = await getBankIntro(bankKey);

                if (isMounted) {
                    setData(result);
                }

            } catch {
                if (isMounted) {
                    setError("Kunde inte hämta bankens introduktion.");
                }

            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        load();
        return () => { isMounted = false; };
    }, [bankKey]);

    // Hooken returnerar allt som en komponent kan vilja läsa.
    return { data, loading, error };
}