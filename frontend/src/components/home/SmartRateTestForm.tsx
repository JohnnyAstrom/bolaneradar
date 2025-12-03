import { useState } from "react";
import type { FC } from "react";
import { runSmartRateTest } from "../../client/smartRateApi";
import type { SmartRateTestResult } from "../../types/smartRate";
import SmartRateTestResultView from "./SmartRateTestResult";

const SmartRateTestForm: FC = () => {

    const [bank, setBank] = useState("");
    const [rate, setRate] = useState("");
    const [result, setResult] = useState<SmartRateTestResult | null>(null);

    async function handleSubmit() {
        // Enkel V1 payload – vi fyller bara i det absolut nödvändiga.
        const payload = {
            bank,
            hasOffer: false,
            currentRate: rate ? Number(rate) : undefined,
            currentRateTerm: "VARIABLE_3M",     // default
            futureRatePreference: "KORT"        // default
        };

        const response = await runSmartRateTest(payload);
        setResult(response);
    }

    return (
        <div className="flex flex-col gap-6 w-full">

            <h2 className="text-2xl font-bold text-text-primary text-center mb-2">
                Smart räntetest
            </h2>

            {/* Form row */}
            <div className="
                flex flex-col lg:flex-row
                items-start lg:items-center
                gap-4 lg:gap-6
                justify-center
            ">

                {/* Bank Dropdown */}
                <select
                    value={bank}
                    onChange={(e) => setBank(e.target.value)}
                    className="
                        border border-border rounded-lg px-4 py-2
                        bg-white text-text-primary w-full lg:w-auto
                        focus:border-border-focus outline-none
                        hover:border-border-hover transition-colors
                    "
                >
                    <option value="">Välj bank</option>
                    <option value="Swedbank">Swedbank</option>
                    <option value="SEB">SEB</option>
                    <option value="Nordea">Nordea</option>
                    <option value="SBAB">SBAB</option>
                </select>

                {/* Ränte-input */}
                <input
                    type="text"
                    value={rate}
                    onChange={(e) => setRate(e.target.value)}
                    placeholder="Ange din ränta"
                    className="
                        border border-border rounded-lg px-4 py-2
                        bg-white text-text-primary w-full lg:w-40
                        focus:border-border-focus outline-none
                        hover:border-border-hover transition-colors
                    "
                />

                {/* Button */}
                <button
                    onClick={handleSubmit}
                    className="
                        bg-primary text-white font-medium
                        px-6 py-2 rounded-lg
                        hover:bg-primary-hover
                        active:bg-primary-active
                        transition-colors
                        w-full lg:w-auto
                    "
                >
                    Kör testet
                </button>

            </div>

            {/* Result section */}
            {result && (
                <div className="mt-8">
                    <SmartRateTestResultView result={result} />
                </div>
            )}
        </div>
    );
};

export default SmartRateTestForm;