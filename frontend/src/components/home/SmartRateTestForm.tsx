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

    // Valideringsfel
    const [errors, setErrors] = useState<Record<string, string>>({});

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

    /** Enkel helper för input-klass med ev. fel */
    function inputClass(errorKey?: string) {
        const hasError = errorKey && errors[errorKey];
        return `border rounded-lg px-4 py-2 bg-white ${
            hasError ? "border-red-500" : "border-border"
        }`;
    }

    /** ----------------------------------------------------
     *  HANDLE SUBMIT — bygger payload + validerar
     * ---------------------------------------------------*/
    async function handleSubmit() {
        const newErrors: Record<string, string> = {};

        const bankId = bankIdMap[bank];
        const bankName = bankNameMap[bank];

        if (!bankId || !bankName) {
            newErrors.bank = "Välj bank.";
        }

        if (!hasOffer) {
            newErrors.hasOffer = "Välj om du har fått ett ränteerbjudande.";
        }

        // FLOW A – ingen offert
        if (hasOffer === "no") {
            if (!currentRate) {
                newErrors.currentRate = "Ange din nuvarande ränta.";
            }
            if (!currentRateType) {
                newErrors.currentRateType = "Välj om räntan är rörlig eller bunden.";
            }
            if (!futureRatePreference) {
                newErrors.futureRatePreference = "Välj vilken ränta du vill jämföra mot.";
            }
        }

        // FLOW B – erbjudanden
        if (hasOffer === "yes") {
            const filledOffers = offers.filter(o => o.term && o.rate);

            if (filledOffers.length === 0) {
                newErrors.offers = "Fyll i bindningstid och ränta för minst ett erbjudande.";
            }
        }

        // Om det finns fel → visa fel och stoppa
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            setResult(null);
            return;
        }

        // Nollställ fel om allt är okej
        setErrors({});

        const payload: SmartRateTestRequest = {
            bankId: bankId!,
            bankName: bankName!,
            hasOffer: hasOffer === "yes",
            loanAmount: loanAmount ? Number(loanAmount) : undefined
        };

        if (hasOffer === "no") {
            payload.userRate = currentRate ? Number(currentRate) : undefined;
            payload.userCurrentTerm = currentRateType || undefined;
            payload.bindingEndDate = bindingEndDate || undefined;
            payload.userPreference = futureRatePreference || undefined;

        } else if (hasOffer === "yes") {
            payload.userRate = undefined;
            payload.userCurrentTerm = undefined;
            payload.bindingEndDate = undefined;
            payload.userPreference = undefined;

            payload.offers = offers
                .filter(o => o.term && o.rate)
                .map(o => ({
                    term: o.term,
                    rate: Number(o.rate)
                }));
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
                className={inputClass("bank")}
            >
                <option value="">Välj bank</option>
                {Object.keys(bankIdMap).map((key) => (
                    <option key={key} value={key}>
                        {bankNameMap[key]}
                    </option>
                ))}
            </select>
            {errors.bank && (
                <p className="text-red-600 text-sm mt-1">{errors.bank}</p>
            )}

            {/* Loan amount */}
            <label className="font-medium">Hur stort är ditt bolån? (Valfritt men bra för mer exakt analys)</label>
            <input
                type="number"
                placeholder="Ex: 2000000"
                value={loanAmount}
                onChange={(e) => setLoanAmount(e.target.value)}
                className={inputClass()}
            />

            {/* Q2 — Offer? */}
            <label className="font-medium">Har du fått ett ränteerbjudande?</label>
            <select
                value={hasOffer}
                onChange={(e) => setHasOffer(e.target.value as "yes" | "no" | "")}
                className={inputClass("hasOffer")}
            >
                <option value="">Välj...</option>
                <option value="yes">Ja</option>
                <option value="no">Nej</option>
            </select>
            {errors.hasOffer && (
                <p className="text-red-600 text-sm mt-1">{errors.hasOffer}</p>
            )}

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
                        className={inputClass("currentRate")}
                    />

                    {/* Hint-text — visas alltid */}
                    <p className="text-gray-500 text-sm">
                        💡 Har du flera bolånedelar med olika räntor? Gör testet en gång per lånedel.
                    </p>

                    {/* Felmeddelande — visas endast vid valideringsfel */}
                    {errors.currentRate && (
                        <p className="text-red-600 text-sm mt-1">{errors.currentRate}</p>
                    )}


                    <label className="font-medium">Har du rörlig eller bunden ränta idag?</label>
                    <select
                        value={currentRateType}
                        onChange={(e) => setCurrentRateType(e.target.value)}
                        className={inputClass("currentRateType")}
                    >
                        <option value="">Välj räntetyp</option>
                        {mortgageTermOptions.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                    {errors.currentRateType && (
                        <p className="text-red-600 text-sm mt-1">{errors.currentRateType}</p>
                    )}

                    {currentRateType.startsWith("FIXED_") && (
                        <>
                            <label className="font-medium">När löper din bindningstid ut? (Valfritt men förbättrar träffsäkerheten i analysen)</label>
                            <input
                                type="date"
                                value={bindingEndDate}
                                onChange={(e) => setBindingEndDate(e.target.value)}
                                className={inputClass("bindingEndDate")}
                            />
                            {errors.bindingEndDate && (
                                <p className="text-red-600 text-sm mt-1">{errors.bindingEndDate}</p>
                            )}
                        </>
                    )}

                    <label className="font-medium">Vilken ränta vill du jämföra mot?</label>
                    <select
                        value={futureRatePreference}
                        onChange={(e) => setFutureRatePreference(e.target.value)}
                        className={inputClass("futureRatePreference")}
                    >
                        <option value="">Välj...</option>
                        <option value="VARIABLE_3M">Rörlig (3 månader)</option>
                        <option value="SHORT">Korta bindningstider (1–3 år)</option>
                        <option value="LONG">Längre bindningstider (4–10 år)</option>
                    </select>
                    {errors.futureRatePreference && (
                        <p className="text-red-600 text-sm mt-1">
                            {errors.futureRatePreference}
                        </p>
                    )}
                </>
            )}

            {/* FLOW B — MULTIPLE OFFERS */}
            {hasOffer === "yes" && (
                <div className="flex flex-col gap-4">
                    {errors.offers && (
                        <p className="text-red-600 text-sm">
                            {errors.offers}
                        </p>
                    )}

                    {offers.map((offer, index) => {
                        const highlightRow = !!errors.offers && (!offer.term || !offer.rate);

                        return (
                            <div
                                key={index}
                                className={`p-4 border rounded-lg bg-gray-50 flex flex-col gap-2 ${
                                    highlightRow ? "border-red-400" : "border-border"
                                }`}
                            >
                                <h4 className="font-semibold">Erbjudande {index + 1}</h4>

                                <select
                                    value={offer.term}
                                    onChange={(e) => updateOffer(index, "term", e.target.value)}
                                    className={inputClass(highlightRow && !offer.term ? "offers" : undefined)}
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
                                    className={inputClass(highlightRow && !offer.rate ? "offers" : undefined)}
                                    placeholder="Ränta, t.ex. 3.15"
                                />

                                {offers.length > 1 && (
                                    <button
                                        className="text-red-500 text-sm underline self-start"
                                        onClick={() => removeOffer(index)}
                                    >
                                        Ta bort erbjudande
                                    </button>
                                )}
                            </div>
                        );
                    })}

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