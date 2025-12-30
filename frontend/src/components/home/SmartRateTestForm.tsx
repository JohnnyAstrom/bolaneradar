import { useState, useEffect } from "react";
import type { FC } from "react";
import { useTranslation } from "react-i18next";
import { runSmartRateTest } from "../../client/smartRateApi";
import type { SmartRateTestResult, SmartRateTestRequest } from "../../types/smartRate";
import SmartRateTestResultView from "./SmartRateTestResult";
import { mortgageTerms } from "../../config/mortgageTerms";

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
    const { t, i18n } = useTranslation();

    // Sparar senaste payload för språkbyte
    const [lastPayload, setLastPayload] = useState<SmartRateTestRequest | null>(null);

    // COMMON
    const [bank, setBank] = useState("");
    const [loanAmount, setLoanAmount] = useState("");
    const [hasOffer, setHasOffer] = useState<"" | "yes" | "no">("");
    const [result, setResult] = useState<SmartRateTestResult | null>(null);
    const [loading, setLoading] = useState(false);

    // FLOW A — NO OFFER
    const [currentRate, setCurrentRate] = useState("");
    const [currentRateType, setCurrentRateType] = useState("");
    const [bindingEndDate, setBindingEndDate] = useState("");
    const [futureRatePreference, setFutureRatePreference] = useState("");

    // FLOW B — MULTIPLE OFFERS
    const [offers, setOffers] = useState<{ term: string; rate: string }[]>([
        { term: "", rate: "" }
    ]);

    const [errors, setErrors] = useState<Record<string, string>>({});

    // Kör om testet automatiskt när språk ändras
    useEffect(() => {
        if (!lastPayload) return;

        const updatedPayload: SmartRateTestRequest = {
            ...lastPayload,
            language: i18n.language === "en" ? "EN" : "SV"
        };

        runSmartRateTest(updatedPayload).then(setResult);

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [i18n.language]);


    function addOfferRow() {
        setOffers([...offers, { term: "", rate: "" }]);
    }

    function updateOffer(index: number, field: "term" | "rate", value: string) {
        const updated = [...offers];
        updated[index][field] = value;
        setOffers(updated);
    }

    function removeOffer(index: number) {
        setOffers(offers.filter((_, i) => i !== index));
    }

    function inputClass(errorKey?: string) {
        const hasError = errorKey && errors[errorKey];
        return `border rounded-lg px-4 py-2 bg-white ${
            hasError ? "border-red-500" : "border-border"
        }`;
    }

    async function handleSubmit() {
        const newErrors: Record<string, string> = {};

        const bankId = bankIdMap[bank];
        const bankName = bankNameMap[bank];

        if (!bankId || !bankName) {
            newErrors.bank = t("smartRate.form.errors.bank");
        }

        if (!hasOffer) {
            newErrors.hasOffer = t("smartRate.form.errors.hasOffer");
        }

        if (hasOffer === "no") {
            if (!currentRate) {
                newErrors.currentRate = t("smartRate.form.errors.currentRate");
            }
            if (!currentRateType) {
                newErrors.currentRateType = t("smartRate.form.errors.currentRateType");
            }
            if (!futureRatePreference) {
                newErrors.futureRatePreference = t("smartRate.form.errors.futurePreference");
            }
        }

        if (hasOffer === "yes") {
            const filledOffers = offers.filter(o => o.term && o.rate);
            if (filledOffers.length === 0) {
                newErrors.offers = t("smartRate.form.errors.offers");
            }
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            setResult(null);
            return;
        }

        setErrors({});

        const payload: SmartRateTestRequest = {
            bankId: bankId!,
            bankName: bankName!,
            hasOffer: hasOffer === "yes",
            loanAmount: loanAmount ? Number(loanAmount) : undefined,
            language: i18n.language === "en" ? "EN" : "SV"
        };

        if (hasOffer === "no") {
            payload.userRate = currentRate ? Number(currentRate) : undefined;
            payload.userCurrentTerm = currentRateType || undefined;
            payload.bindingEndDate = bindingEndDate || undefined;
            payload.userPreference = futureRatePreference || undefined;
        } else {
            payload.offers = offers
                .filter(o => o.term && o.rate)
                .map(o => ({ term: o.term, rate: Number(o.rate) }));
        }

        setLastPayload(payload);

        setLoading(true);

        try {
            const response = await runSmartRateTest(payload);
            setResult(response);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="flex flex-col gap-6 w-full">
            <h2 className="text-2xl font-bold text-text-primary text-center mb-2">
                {t("smartRate.form.title")}
            </h2>

            {/* Bank */}
            <label className="font-medium">{t("smartRate.form.bank.label")}</label>
            <select value={bank} onChange={(e) => setBank(e.target.value)} className={inputClass("bank")}>
                <option value="">{t("smartRate.form.bank.placeholder")}</option>
                {Object.keys(bankIdMap).map((key) => (
                    <option key={key} value={key}>
                        {bankNameMap[key]}
                    </option>
                ))}
            </select>
            {errors.bank && <p className="text-red-600 text-sm mt-1">{errors.bank}</p>}

            {/* Loan amount */}
            <label className="font-medium">{t("smartRate.form.loanAmount.label")}</label>
            <input
                type="number"
                placeholder={t("smartRate.form.loanAmount.placeholder")}
                value={loanAmount}
                onChange={(e) => setLoanAmount(e.target.value)}
                className={inputClass()}
            />

            {/* Offer */}
            <label className="font-medium">{t("smartRate.form.hasOffer.label")}</label>
            <select
                value={hasOffer}
                onChange={(e) => setHasOffer(e.target.value as "yes" | "no" | "")}
                className={inputClass("hasOffer")}
            >
                <option value="">{t("smartRate.form.hasOffer.placeholder")}</option>
                <option value="yes">{t("smartRate.form.hasOffer.yes")}</option>
                <option value="no">{t("smartRate.form.hasOffer.no")}</option>
            </select>
            {errors.hasOffer && <p className="text-red-600 text-sm mt-1">{errors.hasOffer}</p>}

            {/* FLOW A */}
            {hasOffer === "no" && (
                <>
                    <label className="font-medium">{t("smartRate.form.currentRate.label")}</label>
                    <input
                        type="number"
                        step="0.01"
                        placeholder={t("smartRate.form.currentRate.placeholder")}
                        value={currentRate}
                        onChange={(e) => setCurrentRate(e.target.value)}
                        className={inputClass("currentRate")}
                    />
                    <p className="text-gray-500 text-sm">
                        {t("smartRate.form.currentRate.hint")}
                    </p>
                    {errors.currentRate && <p className="text-red-600 text-sm mt-1">{errors.currentRate}</p>}

                    <label className="font-medium">{t("smartRate.form.currentRateType.label")}</label>
                    <select
                        value={currentRateType}
                        onChange={(e) => setCurrentRateType(e.target.value)}
                        className={inputClass("currentRateType")}
                    >
                        <option value="">{t("smartRate.form.currentRateType.placeholder")}</option>
                        {mortgageTerms.map((term) => (
                            <option key={term} value={term}>
                                {t(`mortgage.term.${term}`)}
                            </option>
                        ))}
                    </select>
                    {errors.currentRateType && <p className="text-red-600 text-sm mt-1">{errors.currentRateType}</p>}

                    {currentRateType.startsWith("FIXED_") && (
                        <>
                            <label className="font-medium">{t("smartRate.form.bindingEndDate.label")}</label>
                            <input
                                type="date"
                                value={bindingEndDate}
                                onChange={(e) => setBindingEndDate(e.target.value)}
                                className={inputClass("bindingEndDate")}
                            />
                        </>
                    )}

                    <label className="font-medium">{t("smartRate.form.futurePreference.label")}</label>
                    <select
                        value={futureRatePreference}
                        onChange={(e) => setFutureRatePreference(e.target.value)}
                        className={inputClass("futureRatePreference")}
                    >
                        <option value="">{t("smartRate.form.futurePreference.placeholder")}</option>
                        <option value="VARIABLE_3M">{t("smartRate.form.futurePreference.variable")}</option>
                        <option value="SHORT">{t("smartRate.form.futurePreference.short")}</option>
                        <option value="LONG">{t("smartRate.form.futurePreference.long")}</option>
                    </select>
                    {errors.futureRatePreference && <p className="text-red-600 text-sm mt-1">{errors.futureRatePreference}</p>}
                </>
            )}

            {/* FLOW B */}
            {hasOffer === "yes" && (
                <div className="flex flex-col gap-4">
                    {errors.offers && <p className="text-red-600 text-sm">{errors.offers}</p>}

                    {offers.map((offer, index) => (
                        <div
                            key={index}
                            className={`p-4 border rounded-lg bg-gray-50 flex flex-col gap-2 ${
                                errors.offers && (!offer.term || !offer.rate)
                                    ? "border-red-400"
                                    : "border-border"
                            }`}
                        >
                            <h4 className="font-semibold">
                                {t("smartRate.form.offer.title", { index: index + 1 })}
                            </h4>

                            <select
                                value={offer.term}
                                onChange={(e) => updateOffer(index, "term", e.target.value)}
                                className={inputClass(errors.offers && !offer.term ? "offers" : undefined)}
                            >
                                <option value="">{t("smartRate.form.offer.termPlaceholder")}</option>
                                {mortgageTerms.map((term) => (
                                    <option key={term} value={term}>
                                        {t(`mortgage.term.${term}`)}
                                    </option>
                                ))}
                            </select>

                            <input
                                type="number"
                                step="0.01"
                                value={offer.rate}
                                onChange={(e) => updateOffer(index, "rate", e.target.value)}
                                className={inputClass(errors.offers && !offer.rate ? "offers" : undefined)}
                                placeholder={t("smartRate.form.offer.ratePlaceholder")}
                            />

                            {offers.length > 1 && (
                                <button
                                    className="text-red-500 text-sm underline self-start"
                                    onClick={() => removeOffer(index)}
                                >
                                    {t("smartRate.form.offer.remove")}
                                </button>
                            )}
                        </div>
                    ))}

                    <button
                        onClick={addOfferRow}
                        className="bg-blue-100 text-blue-700 px-4 py-2 rounded-lg"
                    >
                        {t("smartRate.form.offer.add")}
                    </button>
                </div>
            )}

            {/* Submit-knapp */}
            {(hasOffer === "yes" || hasOffer === "no") && (
                <button
                    onClick={handleSubmit}
                    disabled={loading}
                    className={`mt-4 bg-primary text-white px-6 py-2 rounded-lg ${
                        loading ? "opacity-60 cursor-not-allowed" : ""
                    }`}
                >
                    {loading
                        ? t("smartRate.form.loading", "Analyserar din ränta…")
                        : t("smartRate.form.submit")}
                </button>
            )}

            {/* Loader + förklarande text */}
            {loading && (
                <div className="mt-6 text-center">
                    <div className="animate-spin mx-auto mb-3 h-6 w-6 border-2 border-primary border-t-transparent rounded-full" />
                    <p className="text-sm text-text-secondary">
                        {t(
                            "smartRate.form.loadingHint",
                            "Analyserar din ränta – detta kan ta upp till 20 sekunder"
                        )}
                    </p>
                </div>
            )}

            {/* Resultat */}
            {result && (
                <div className="mt-8">
                    <SmartRateTestResultView result={result} />
                </div>
            )}
        </div>
    );
};

export default SmartRateTestForm;