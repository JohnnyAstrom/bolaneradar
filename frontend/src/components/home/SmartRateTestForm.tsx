import { useState } from "react";
import type { FC } from "react";
import { runSmartRateTest } from "../../client/smartRateApi";
import type { SmartRateTestResult } from "../../types/smartRate";
import SmartRateTestResultView from "./SmartRateTestResult";
import { mortgageTermOptions } from "../../config/mortgageTerms";

const SmartRateTestForm: FC = () => {

    // COMMON
    const [bank, setBank] = useState("");
    const [hasOffer, setHasOffer] = useState<"" | "yes" | "no">("");
    const [result, setResult] = useState<SmartRateTestResult | null>(null);

    // FLOW A — NO OFFER
    const [currentRate, setCurrentRate] = useState("");
    const [currentRateType, setCurrentRateType] = useState("");

    const [rateChangeDate, setRateChangeDate] = useState("");
    const [bindingEndDate, setBindingEndDate] = useState("");

    const [futureRatePreference, setFutureRatePreference] = useState("");

    // FLOW B — HAS OFFER (no Q6B anymore)
    const [offerBindingPeriod, setOfferBindingPeriod] = useState("");
    const [offerRate, setOfferRate] = useState("");

    async function handleSubmit() {
        let payload: any;

        if (hasOffer === "no") {
            payload = {
                bank,
                hasOffer: false,
                currentRate: currentRate ? Number(currentRate) : undefined,
                currentRateType,
                rateChangeDate: rateChangeDate || undefined,
                bindingEndDate: bindingEndDate || undefined,
                futureRatePreference
            };
        } else {
            payload = {
                bank,
                hasOffer: true,
                offerBindingPeriod,
                offerRate: offerRate ? Number(offerRate) : undefined
            };
        }

        const response = await runSmartRateTest(payload);
        setResult(response);
    }

    return (
        <div className="flex flex-col gap-6 w-full">
            <h2 className="text-2xl font-bold text-text-primary text-center mb-2">
                Smart räntetest
            </h2>

            {/* Q1 */}
            <label className="font-medium">Vilken bank har du ditt bolån hos?</label>
            <select
                value={bank}
                onChange={(e) => setBank(e.target.value)}
                className="border border-border rounded-lg px-4 py-2 bg-white"
            >
                <option value="">Välj bank</option>
                <option value="swedbank">Swedbank</option>
                <option value="nordea">Nordea</option>
                <option value="seb">SEB</option>
                <option value="handelsbanken">Handelsbanken</option>
                <option value="sbab">SBAB</option>
                <option value="icabanken">Ica Banken</option>
                <option value="lansforsakringarbank">Länsförsäkringar Bank</option>
                <option value="danskebank">Danske Bank</option>
                <option value="skandiabanken">SkandiaBanken</option>
                <option value="landshypotekbank">Lankshypotek Bank</option>
                <option value="alandsbanken">Ålandsbanken</option>
                <option value="ikanobank">Ikano Bank</option>
                <option value="hypoteket">Hypoteket</option>
                <option value="stabelo">Stabelo</option>
                <option value="annanbank">Annan Bank</option>
            </select>

            {/* Q2 */}
            <label className="font-medium">Har du fått ett ränteerbjudande?</label>
            <select
                value={hasOffer}
                onChange={(e) => setHasOffer(e.target.value as any)}
                className="border border-border rounded-lg px-4 py-2 bg-white"
            >
                <option value="">Välj...</option>
                <option value="yes">Ja</option>
                <option value="no">Nej</option>
            </select>

            {/* FLOW A — INGET ERBJUDANDE */}
            {hasOffer === "no" && (
                <>
                    {/* Q3 */}
                    <label className="font-medium">Vilken ränta har du idag?</label>
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Ex: 4.15"
                        value={currentRate}
                        onChange={(e) => setCurrentRate(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                    />

                    {/* Q4 */}
                    <label className="font-medium">
                        Har du rörlig eller bunden ränta idag?
                    </label>
                    <select
                        value={currentRateType}
                        onChange={(e) => setCurrentRateType(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                    >
                        <option value="">Välj räntetyp</option>
                        {mortgageTermOptions.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>

                    {/* Q5A — rörlig */}
                    {currentRateType === "VARIABLE_3M" && (
                        <>
                            <label className="font-medium">
                                När sker din nästa ränteändringsdag? (valfri)
                            </label>
                            <input
                                type="date"
                                value={rateChangeDate}
                                onChange={(e) => setRateChangeDate(e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                            />
                        </>
                    )}

                    {/* Q5B — bunden */}
                    {currentRateType.startsWith("FIXED_") && (
                        <>
                            <label className="font-medium">
                                När löper din bindningstid ut? (valfri)
                            </label>
                            <input
                                type="date"
                                value={bindingEndDate}
                                onChange={(e) => setBindingEndDate(e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                            />
                        </>
                    )}

                    {/* Q6A */}
                    {currentRateType !== "" && (
                        <>
                            <label className="font-medium">
                                Vilken typ av ränta vill du gå vidare med och jämföra?
                            </label>
                            <select
                                value={futureRatePreference}
                                onChange={(e) => setFutureRatePreference(e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                            >
                                <option value="">Välj...</option>
                                <option value="VARIABLE_3M">Rörlig (3 månader)</option>
                                <option value="SHORT">Korta bindningstider (1 år - 3 år)</option>
                                <option value="LONG">Längre bindningstider (4 år - 10 år)</option>
                            </select>
                        </>
                    )}
                </>
            )}

            {/* FLOW B — ERBJUDANDE */}
            {hasOffer === "yes" && (
                <>
                    {/* Q3A */}
                    <label className="font-medium">
                        Vilken bindningstid gäller erbjudandet?
                    </label>
                    <select
                        value={offerBindingPeriod}
                        onChange={(e) => setOfferBindingPeriod(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                    >
                        <option value="">Välj bindningstid</option>
                        {mortgageTermOptions.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>

                    {/* Q3B */}
                    <label className="font-medium">Vilken ränta har du blivit erbjuden?</label>
                    <input
                        type="number"
                        step="0.01"
                        value={offerRate}
                        onChange={(e) => setOfferRate(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                        placeholder="Ex: 4.15"
                    />
                </>
            )}

            {/* SUBMIT */}
            {(hasOffer === "yes" || hasOffer === "no") && (
                <button
                    onClick={handleSubmit}
                    className="mt-4 bg-primary text-white px-6 py-2 rounded-lg"
                >
                    Skicka in testet
                </button>
            )}

            {/* RESULT */}
            {result && (
                <div className="mt-8">
                    <SmartRateTestResultView result={result} />
                </div>
            )}
        </div>
    );
};

export default SmartRateTestForm;