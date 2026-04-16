import type { ReactNode, HTMLAttributes } from "react";

interface SectionProps extends HTMLAttributes<HTMLElement> {
    title?: string;
    children: ReactNode;
    className?: string;
    contentClassName?: string;
}

export default function Section({
    title,
    children,
    className = "",
    contentClassName = "",
    ...rest
}: SectionProps) {
    return (
        <section
            {...rest}   // ← Detta gör att id, style, aria osv funkar
            className={`mb-4 sm:mb-8 px-1 sm:px-6 ${className}`}
        >
            <div className={`bg-white border border-border rounded-lg p-2 sm:p-6 shadow-sm ${contentClassName}`}>
                {title && (
                    <h2 className="text-xl font-semibold text-text-primary mb-4">
                        {title}
                    </h2>
                )}

                {children}
            </div>
        </section>
    );
}
