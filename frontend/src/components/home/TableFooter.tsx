import type { FC } from "react";

const TableFooter: FC = () => {
    return (
        <div className="mt-4 text-xs text-text-secondary leading-relaxed">
            <p>Källa: Bankernas publika webbsidor</p>
            <p>Senast uppdaterad av BolåneRadar: 2025-10-25 09:00</p>
        </div>
    );
};

export default TableFooter;