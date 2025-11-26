import type { FC } from "react";

const BankGraphSection: FC = () => {
    return (
        <div className="mt-12">

            {/* Titel */}
            <h2 className="text-2xl font-semibold text-text-primary text-center mb-2">
                Historisk utveckling
            </h2>

            {/* Undertext */}
            <p className="text-center text-text-secondary text-sm mb-6">
                Visar bankens genomsnittliga boränta de senaste 12 månaderna.<br />
                Välj bindningstid i dropdown menyn nedan:
            </p>

            {/* Dropdown */}
            <div className="flex justify-center mb-6">
                <select
                    className="
                        px-4 py-2 border border-border rounded-md bg-white
                        text-text-primary text-sm
                        hover:border-icon-neutral focus:outline-none focus:ring-2 focus:ring-primary-light
                    "
                >
                    <option>Välj bindningstid</option>
                </select>
            </div>

            {/* Placeholder-grafcontainer */}
            <div
                className="
                    h-80 border border-border rounded-lg
                    bg-white flex items-center justify-center
                    text-text-secondary text-sm
                "
            >
                Graf visas här
            </div>

        </div>
    );
};

export default BankGraphSection;