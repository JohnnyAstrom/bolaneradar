import type { FC } from "react";
import { useState } from "react";
import { brandConfig } from "../../config/brandConfig";

interface BankLogoProps {
    src: string;
    alt: string;
    bankKey: string;
    variant?: "default" | "table";
}

const BankLogo: FC<BankLogoProps> = ({
    src,
    alt,
    bankKey,
    variant = "default",
}) => {
    const [loaded, setLoaded] = useState(false);

    const config = brandConfig[bankKey] ?? {};
    const maxHeight =
        variant === "table"
            ? (config.tableMaxHeight ?? Math.min(config.maxHeight ?? 32, 32))
            : (config.maxHeight ?? 56);
    const tablePadding = 0;
    const tableMaxWidth = config.tableMaxWidth ?? 112;
    const tableScale = config.tableScale ?? 1;

    const containerClasses =
        variant === "table"
            ? "inline-flex items-center justify-start w-[120px] h-[40px]"
            : "inline-flex items-center mb-6";

    return (
        <div
            className={containerClasses}
            style={{
                minHeight: `${maxHeight}px`,
                padding:
                    variant === "table"
                        ? `${tablePadding}px`
                        : (config.padding ? `${config.padding}px` : "0px"),
                background:
                    variant === "table"
                        ? "transparent"
                        : (config.background ?? "transparent"),
                borderRadius: "6px",
            }}
        >
            <img
                src={src}
                alt={alt}
                title={alt}
                loading={variant === "table" ? "lazy" : "eager"}
                decoding={variant === "table" ? "async" : "sync"}
                width={200}
                height={maxHeight}
                onLoad={() => setLoaded(true)}
                style={{
                    height: variant === "table" ? "100%" : `${maxHeight}px`,
                    width: variant === "table" ? "100%" : "auto",
                    maxWidth: variant === "table" ? `${tableMaxWidth}px` : "100%",
                    maxHeight: `${maxHeight}px`,
                    display: "block",
                    objectFit: "contain",
                    objectPosition: "left center",
                    transform: variant === "table" ? `scale(${tableScale})` : "none",
                    transformOrigin: "left center",
                    opacity: loaded ? 1 : 0,
                    transition: "opacity 150ms ease-out",
                }}
            />
        </div>
    );
};

export default BankLogo;
