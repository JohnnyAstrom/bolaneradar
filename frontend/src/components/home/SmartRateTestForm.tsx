import { useState } from "react";
import type { FC } from "react";
import { runSmartRateTest } from "../../client/smartRateApi";
import type { SmartRateTestResult, SmartRateTestRequest } from "../../types/smartRate";
import SmartRateTestResultView from "./SmartRateTestResult";
import { mortgageTermOptions } from "../../config/mortgageTerms";

/** ---------------------------------------
 *  BANK-ID + RIKTIGT NAMN FÖR VISNING
 * --------------------------------------*/
const bankIdMap: Record<string, number> = {
    swedbank: 1,
    nordea: 2,
    seb: 3,
    handelsbanken: 4,
    sbab: 5,
    icabanken: 6,
    lansforsakringarbank: 7,
    danskebank: 8,
    skandiabanken: 9,
    landshypotekbank: 10,
    alandsbanken: 11,
    ikanobank: 12,
    hypoteket: 13,
    stabelo: 14,
    annanbank: 999
};

const bankNameMap: Record<string, string> = {
    swedbank: "Swedbank",
    nordea: "Nordea",
    seb: "SEB",
    handelsbanken: "Handelsbanken",
    sbab: "SBAB",
    icabanken: "ICA Banken",
    lansforsakringarbank: "Länsförsäkringar Bank",
    danskebank: "Danske Bank",
    skandiabanken: "SkandiaBanken",
    landshypotekbank: "Landshypotek Bank",
    alandsbanken: "Ålandsbanken",
    ikanobank: "Ikano Bank",
    hypoteket: "Hypoteket",
    stabelo: "Stabelo",
    annanbank: "Annan bank"
};

const SmartRateTestForm: FC = () => {

    // COMMON
    const [bank, setBank] = useState("");
    const [loanAmount, setLoanAmount] = useState("");
    const [hasOffer, setHasOffer] = useState<"" | "yes" | "no">("");
    const [result, setResult] = useState<SmartRateTestResult | null>(null);

    // FLOW A — NO OFFER
    const [currentRate, setCurrentRate] = useState("");
    const [currentRateType, setCurrentRateType] = useState("");
    const [bindingEndDate, setBindingEndDate] = useState("");
    const [futureRatePreference, setFutureRatePreference] = useState("");

    // FLOW B — MULTIPLE OFFERS
    const [offers, setOffers] = useState<{ term: string; rate: string }[]>([
        { term: "", rate: "" }
    ]);

    // Add new empty offer row
    function addOfferRow() {
        setOffers([...offers, { term: "", rate: "" }]);
    }

    // Update a specific offer row field
    function updateOffer(index: number, field: "term" | "rate", value: string) {
        const updated = [...offers];
        updated[index][field] = value;
        setOffers(updated);
    }

    // Remove a specific offer row
    function removeOffer(index: number) {
        setOffers(offers.filter((_, i) => i !== index));
    }

    /** ----------------------------------------------------
     *  HANDLE SUBMIT — bygger payload exakt för backend V5
     * ---------------------------------------------------*/
    async function handleSubmit() {
        const bankId = bankIdMap[bank];
        const bankName = bankNameMap[bank];

        if (!bankId || !bankName) {
            console.error("Ogiltig bank vald");
            return;
        }

        const payload: SmartRateTestRequest = {
            bankId,
            bankName,
            hasOffer: hasOffer === "yes",
            loanAmount: loanAmount ? Number(loanAmount) : undefined
        };

        if (hasOffer === "no") {
            // Endast Flow A-fält
            payload.userRate = currentRate ? Number(currentRate) : undefined;
            payload.userCurrentTerm = currentRateType || undefined;
            payload.bindingEndDate = bindingEndDate || undefined;
            payload.userPreference = futureRatePreference || undefined;

        } else if (hasOffer === "yes") {
            // Nollställ Flow A-fält
            payload.userRate = undefined;
            payload.userCurrentTerm = undefined;
            payload.bindingEndDate = undefined;
            payload.userPreference = undefined;

            // Sätt offers
            payload.offers = offers
                .filter(o => o.term && o.rate)
                .map(o => ({
                    term: o.term,
                    rate: Number(o.rate)
                }));

            // Validering: minst 1 erbjudande måste vara ifyllt
            if (!payload.offers || payload.offers.length === 0) {
                alert("Fyll i minst ett ränteerbjudande.");
                return;
            }
        }

        const response = await runSmartRateTest(payload);
        setResult(response);
    }

    return (
        <div className="flex flex-col gap-6 w-full">
            <h2 className="text-2xl font-bold text-text-primary text-center mb-2">
                Smart räntetest
            </h2>

            {/* Q1 — Bank */}
            <label className="font-medium">Vilken bank har du ditt bolån hos?</label>
            <select
                value={bank}
                onChange={(e) => setBank(e.target.value)}
                className="border border-border rounded-lg px-4 py-2 bg-white"
            >
                <option value="">Välj bank</option>
                {Object.keys(bankIdMap).map((key) => (
                    <option key={key} value={key}>
                        {bankNameMap[key]}
                    </option>
                ))}
            </select>

            {/* Loan amount */}
            <label className="font-medium">Hur stort är ditt bolån? (frivilligt)</label>
            <input
                type="number"
                placeholder="Ex: 2000000"
                value={loanAmount}
                onChange={(e) => setLoanAmount(e.target.value)}
                className="border border-border rounded-lg px-4 py-2 bg-white"
            />

            {/* Q2 — Offer? */}
            <label className="font-medium">Har du fått ett ränteerbjudande?</label>
            <select
                value={hasOffer}
                onChange={(e) => setHasOffer(e.target.value as "yes" | "no" | "")}
                className="border border-border rounded-lg px-4 py-2 bg-white"
            >
                <option value="">Välj...</option>
                <option value="yes">Ja</option>
                <option value="no">Nej</option>
            </select>

            {/* FLOW A — NO OFFER */}
            {hasOffer === "no" && (
                <>
                    <label className="font-medium">Vilken ränta har du idag?</label>
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Ex: 3.15"
                        value={currentRate}
                        onChange={(e) => setCurrentRate(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                    />

                    <label className="font-medium">Har du rörlig eller bunden ränta idag?</label>
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

                    {currentRateType.startsWith("FIXED_") && (
                        <>
                            <label className="font-medium">När löper din bindningstid ut?</label>
                            <input
                                type="date"
                                value={bindingEndDate}
                                onChange={(e) => setBindingEndDate(e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                            />
                        </>
                    )}

                    <label className="font-medium">Vilken ränta vill du jämföra mot?</label>
                    <select
                        value={futureRatePreference}
                        onChange={(e) => setFutureRatePreference(e.target.value)}
                        className="border border-border rounded-lg px-4 py-2 bg-white"
                    >
                        <option value="">Välj...</option>
                        <option value="VARIABLE_3M">Rörlig (3 månader)</option>
                        <option value="SHORT">Korta bindningstider (1–3 år)</option>
                        <option value="LONG">Längre bindningstider (4–10 år)</option>
                    </select>
                </>
            )}

            {/* FLOW B — MULTIPLE OFFERS */}
            {hasOffer === "yes" && (
                <div className="flex flex-col gap-4">
                    {offers.map((offer, index) => (
                        <div
                            key={index}
                            className="p-4 border border-border rounded-lg bg-gray-50 flex flex-col gap-2"
                        >
                            <h4 className="font-semibold">Erbjudande {index + 1}</h4>

                            <select
                                value={offer.term}
                                onChange={(e) => updateOffer(index, "term", e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                            >
                                <option value="">Välj bindningstid</option>
                                {mortgageTermOptions.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>

                            <input
                                type="number"
                                step="0.01"
                                value={offer.rate}
                                onChange={(e) => updateOffer(index, "rate", e.target.value)}
                                className="border border-border rounded-lg px-4 py-2 bg-white"
                                placeholder="Ränta, t.ex. 3.15"
                            />

                            {offers.length > 1 && (
                                <button
                                    className="text-red-500 text-sm underline"
                                    onClick={() => removeOffer(index)}
                                >
                                    Ta bort erbjudande
                                </button>
                            )}
                        </div>
                    ))}

                    <button
                        onClick={addOfferRow}
                        className="bg-blue-100 text-blue-700 px-4 py-2 rounded-lg"
                    >
                        Lägg till fler erbjudanden
                    </button>
                </div>
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