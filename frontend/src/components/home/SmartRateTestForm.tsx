import { useState, useEffect, useRef } from "react";
import type { FC, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { BadgeInfo, CircleAlert, Plus, Sparkles, Trash2 } from "lucide-react";
import { runSmartRateTest } from "../../services/smartRateApi";
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

interface Props {
    onScrollToRates: () => void;
}

interface OfferRow {
    term: string;
    rate: string;
}

interface FieldProps {
    label: string;
    error?: string;
    children: ReactNode;
    hint?: ReactNode;
}

const FormField: FC<FieldProps> = ({ label, error, children, hint }) => (
    <div className="space-y-2.5">
        <label className="font-semibold text-[0.98rem] text-text-primary">{label}</label>
        {children}
        {hint}
        {error && <p className="text-red-600 text-sm">{error}</p>}
    </div>
);

const SectionIntro: FC<{ title: string; description?: string }> = ({ title, description }) => (
    <div className="space-y-1">
        <h3 className="text-lg font-semibold tracking-tight text-text-primary">{title}</h3>
        {description && (
            <p className="text-sm leading-6 text-text-secondary">{description}</p>
        )}
    </div>
);

const SmartRateTestForm: FC<Props> = ({ onScrollToRates }) => {
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
    const [offers, setOffers] = useState<OfferRow[]>([
        { term: "", rate: "" }
    ]);

    // Sparar valideringsfel per fält (nyckel = fältnamn, värde = felmeddelande)
    const [errors, setErrors] = useState<Record<string, string>>({});

    // Visar vänligt felmeddelande om analysen misslyckas
    const [submitError, setSubmitError] = useState<string | null>(null);

    const resultRef = useRef<HTMLDivElement | null>(null);

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
        return `w-full border rounded-2xl px-4 py-3 bg-white text-text-primary shadow-sm transition-colors ${
            hasError
                ? "border-red-500 focus:border-red-500 focus:ring-red-100"
                : "border-border focus:border-primary/40 focus:ring-primary/10"
        }`;
    }

    async function handleSubmit() {
        const newErrors: Record<string, string> = {};
        setSubmitError(null);

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

            requestAnimationFrame(() => {
                resultRef.current?.scrollIntoView({
                    behavior: "smooth",
                    block: "start",
                });
            });
        } catch (e) {
            // Render kan vara långsam eller tillfälligt otillgänglig
            // Visa ett lugnt och begripligt felmeddelande istället för att bara logga
            setSubmitError(
                t(
                    "smartRate.form.errorTimeout",
                    "Analysen tog längre tid än väntat. Försök igen om en stund."
                )
            );
            console.error("SmartRate request failed", e);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="w-full">
            <div className="rounded-[28px] border border-border bg-white shadow-sm overflow-hidden">
                <div className="border-b border-slate-200 bg-gradient-to-r from-slate-50 via-white to-sky-50 px-6 py-6 sm:px-7">
                    <div className="flex items-start gap-4">
                        <div className="hidden sm:inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                            <Sparkles size={22} />
                        </div>
                        <div className="space-y-2">
                            <h2 className="text-3xl font-bold tracking-tight text-text-primary">
                                {t("smartRate.form.title")}
                            </h2>
                            <p className="max-w-2xl text-sm leading-6 text-text-secondary">
                                {t(
                                    "smartRate.form.intro",
                                    "Fyll i dina uppgifter så analyserar vi hur din ränta eller dina erbjudanden står sig mot marknaden just nu."
                                )}
                            </p>
                        </div>
                    </div>
                </div>

                <div className="flex flex-col gap-8 px-6 py-6 sm:px-7 sm:py-7">
                    <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr] lg:items-start">
                        <div className="px-2 sm:px-3">
                            <SectionIntro
                                title={t("smartRate.form.sections.basicsTitle", "Grunduppgifter")}
                                description={t("smartRate.form.sections.basicsDescription", "Vi behöver veta vilken bank du har idag och om du vill analysera din nuvarande ränta eller konkreta erbjudanden.")}
                            />

                            <div className="mt-5 grid gap-5">
                                <FormField label={t("smartRate.form.bank.label")} error={errors.bank}>
                                    <select value={bank} onChange={(e) => setBank(e.target.value)} className={inputClass("bank")}>
                                        <option value="">{t("smartRate.form.bank.placeholder")}</option>
                                        {Object.keys(bankIdMap).map((key) => (
                                            <option key={key} value={key}>
                                                {bankNameMap[key]}
                                            </option>
                                        ))}
                                    </select>
                                </FormField>

                                <FormField label={t("smartRate.form.loanAmount.label")}>
                                    <input
                                        type="number"
                                        placeholder={t("smartRate.form.loanAmount.placeholder")}
                                        value={loanAmount}
                                        onChange={(e) => setLoanAmount(e.target.value)}
                                        className={inputClass()}
                                    />
                                </FormField>

                                <FormField label={t("smartRate.form.hasOffer.label")} error={errors.hasOffer}>
                                    <select
                                        value={hasOffer}
                                        onChange={(e) => setHasOffer(e.target.value as "yes" | "no" | "")}
                                        className={inputClass("hasOffer")}
                                    >
                                        <option value="">{t("smartRate.form.hasOffer.placeholder")}</option>
                                        <option value="yes">{t("smartRate.form.hasOffer.yes")}</option>
                                        <option value="no">{t("smartRate.form.hasOffer.no")}</option>
                                    </select>
                                </FormField>
                            </div>
                        </div>

                        <div className="rounded-3xl border border-sky-100 bg-sky-50/70 p-5 sm:p-6 self-start">
                            <div className="flex items-start gap-3">
                                <div className="mt-0.5 text-primary">
                                    <BadgeInfo size={18} />
                                </div>
                                <div className="space-y-2">
                                    <h3 className="text-base font-semibold text-text-primary">
                                        {t("smartRate.form.helpTitle", "Så fungerar testet")}
                                    </h3>
                                    <p className="text-sm leading-6 text-text-secondary">
                                        {hasOffer === "yes"
                                            ? t("smartRate.form.helpOffers", "Lägg in ett eller flera erbjudanden så jämför vi dem mot marknadens nivåer och visar vilket som står sig bäst.")
                                            : t("smartRate.form.helpCurrent", "Ange din nuvarande ränta och vad du vill jämföra mot, så får du en snabb bedömning av hur din nivå står sig idag.")}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {hasOffer === "no" && (
                        <div className="border-t border-slate-200 pt-8 px-2 sm:px-3">
                            <SectionIntro
                                title={t("smartRate.form.sections.currentRateTitle", "Din nuvarande ränta")}
                                description={t("smartRate.form.sections.currentRateDescription", "Det här flödet passar dig som vill bedöma din nuvarande nivå och se hur den står sig mot marknaden.")}
                            />

                            <div className="mt-5 grid gap-5">
                                <FormField
                                    label={t("smartRate.form.currentRate.label")}
                                    error={errors.currentRate}
                                    hint={
                                        <div className="flex items-start gap-2 rounded-2xl bg-amber-50 px-3 py-2 text-sm text-amber-900">
                                            <span className="mt-0.5">💡</span>
                                            <span>{t("smartRate.form.currentRate.hint")}</span>
                                        </div>
                                    }
                                >
                                    <input
                                        type="number"
                                        step="0.01"
                                        placeholder={t("smartRate.form.currentRate.placeholder")}
                                        value={currentRate}
                                        onChange={(e) => setCurrentRate(e.target.value)}
                                        className={inputClass("currentRate")}
                                    />
                                </FormField>

                                <div className="grid gap-5 lg:grid-cols-2">
                                    <FormField label={t("smartRate.form.currentRateType.label")} error={errors.currentRateType}>
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
                                    </FormField>

                                    {currentRateType.startsWith("FIXED_") && (
                                        <FormField label={t("smartRate.form.bindingEndDate.label")}>
                                            <input
                                                type="date"
                                                value={bindingEndDate}
                                                onChange={(e) => setBindingEndDate(e.target.value)}
                                                className={inputClass("bindingEndDate")}
                                            />
                                        </FormField>
                                    )}
                                </div>

                                <FormField label={t("smartRate.form.futurePreference.label")} error={errors.futureRatePreference}>
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
                                </FormField>
                            </div>
                        </div>
                    )}

                    {hasOffer === "yes" && (
                        <div className="border-t border-slate-200 pt-8 px-2 sm:px-3">
                            <SectionIntro
                                title={t("smartRate.form.sections.offersTitle", "Dina erbjudanden")}
                                description={t("smartRate.form.sections.offersDescription", "Lägg till ett eller flera ränteerbjudanden så jämför vi dem mot marknaden och mot varandra.")}
                            />

                            <div className="mt-5 flex flex-col gap-4">
                                {errors.offers && (
                                    <div className="flex items-start gap-2 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                                        <CircleAlert size={16} className="mt-0.5 shrink-0" />
                                        <span>{errors.offers}</span>
                                    </div>
                                )}

                                {offers.map((offer, index) => (
                                    <div
                                        key={index}
                                        className={`rounded-3xl border p-4 sm:p-5 ${
                                            errors.offers && (!offer.term || !offer.rate)
                                                ? "border-red-300 bg-red-50/30"
                                                : "border-slate-200 bg-slate-50/60"
                                        }`}
                                    >
                                        <div className="flex items-center justify-between gap-3">
                                            <h4 className="text-base font-semibold text-text-primary">
                                                {t("smartRate.form.offer.title", { index: index + 1 })}
                                            </h4>
                                            {offers.length > 1 && (
                                                <button
                                                    type="button"
                                                    className="inline-flex items-center gap-1 rounded-full px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
                                                    onClick={() => removeOffer(index)}
                                                >
                                                    <Trash2 size={14} />
                                                    {t("smartRate.form.offer.remove")}
                                                </button>
                                            )}
                                        </div>

                                        <div className="mt-4 grid gap-3 lg:grid-cols-[1fr_0.85fr]">
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
                                        </div>
                                    </div>
                                ))}

                                <button
                                    type="button"
                                    onClick={addOfferRow}
                                    className="inline-flex items-center justify-center gap-2 rounded-2xl border border-sky-200 bg-sky-50 px-4 py-3 font-medium text-primary transition-colors hover:bg-sky-100"
                                >
                                    <Plus size={16} />
                                    {t("smartRate.form.offer.add")}
                                </button>
                            </div>
                        </div>
                    )}

                    {(hasOffer === "yes" || hasOffer === "no") && (
                        <div className="border-t border-slate-200 pt-8 px-2 sm:px-3">
                            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                                <p className="text-sm leading-6 text-text-secondary max-w-xl">
                                    {t(
                                        "smartRate.form.submitHelp",
                                        "När du skickar in testet analyserar vi din nivå mot aktuella marknadsdata och visar ett resultat som är lätt att tolka."
                                    )}
                                </p>
                                <button
                                    onClick={handleSubmit}
                                    disabled={loading}
                                    className={`inline-flex items-center justify-center rounded-2xl bg-primary px-6 py-3.5 font-semibold text-white shadow-sm transition-all sm:min-w-[190px] ${
                                        loading ? "opacity-60 cursor-not-allowed" : "hover:bg-primary-hover hover:-translate-y-[1px]"
                                    }`}
                                >
                                    {loading
                                        ? t("smartRate.form.loading", "Analyserar din ränta…")
                                        : t("smartRate.form.submit")}
                                </button>
                            </div>
                        </div>
                    )}

                    {loading && (
                        <div className="rounded-3xl border border-sky-200 bg-sky-50 px-5 py-5 text-center">
                            <div className="animate-spin mx-auto mb-3 h-7 w-7 border-2 border-primary border-t-transparent rounded-full" />
                            <p className="text-sm text-text-secondary">
                                {t(
                                    "smartRate.form.loadingHint",
                                    "Analyserar din ränta – detta kan ta upp till 20 sekunder"
                                )}
                            </p>
                        </div>
                    )}

                    {submitError && (
                        <div className="rounded-3xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">
                            {submitError}
                        </div>
                    )}
                </div>
            </div>

            {result && (
                <div ref={resultRef} className="mt-8">
                    <SmartRateTestResultView
                        result={result}
                        onScrollToRates={onScrollToRates}
                    />
                </div>
            )}
        </div>
    );
};

export default SmartRateTestForm;
