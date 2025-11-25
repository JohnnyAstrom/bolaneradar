import type { FC } from "react";

const SmartRateTestForm: FC = () => {
    return (
        <div className="flex flex-col gap-6 w-full">

            {/* Title */}
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
                    className="
            border border-border rounded-lg px-4 py-2
            bg-white text-text-primary w-full lg:w-auto
            focus:border-border-focus outline-none
            hover:border-border-hover transition-colors
          "
                >
                    <option>Välj bank</option>
                </select>

                {/* Ränte-input */}
                <input
                    type="text"
                    placeholder="Ange din ränta"
                    className="
            border border-border rounded-lg px-4 py-2
            bg-white text-text-primary w-full lg:w-40
            focus:border-border-focus outline-none
            hover:border-border-hover transition-colors
          "
                />

                {/* Ränteändringsdag Dropdown */}
                <select
                    className="
            border border-border rounded-lg px-4 py-2
            bg-white text-text-primary w-full lg:w-auto
            focus:border-border-focus outline-none
            hover:border-border-hover transition-colors
          "
                >
                    <option>Ränteändringsdag</option>
                </select>

                {/* Button */}
                <button
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
        </div>
    );
};

export default SmartRateTestForm;