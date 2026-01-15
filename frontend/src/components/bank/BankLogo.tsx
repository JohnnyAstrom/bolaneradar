import type { FC } from "react";
import { useState } from "react";
import { brandConfig } from "../../config/brandConfig";

interface BankLogoProps {
    src: string;
    alt: string;
    bankKey: string;
}

const BankLogo: FC<BankLogoProps> = ({ src, alt, bankKey }) => {
    const [loaded, setLoaded] = useState(false);

    const config = brandConfig[bankKey] ?? {};
    const maxH = config.maxHeight ? `${config.maxHeight}px` : "56px";

    return (
        <div
            className="inline-flex items-center mb-6"
            style={{
                minHeight: maxH, // 👉 förhindrar layout-shift
                padding: config.padding ? `${config.padding}px` : "0px",
                background: config.background ?? "transparent",
                borderRadius: "6px",
            }}
        >
            <img
                src={src}
                alt={alt}
                loading="eager"
                decoding="sync"
                width={200}
                height={56}
                onLoad={() => setLoaded(true)}
                style={{
                    height: maxH,
                    width: "auto",
                    display: "block",
                    objectFit: "contain",
                    opacity: loaded ? 1 : 0,
                    transition: "opacity 150ms ease-out",
                }}
            />
        </div>
    );
};

export default BankLogo;