import type { ReactNode } from "react";

interface SectionProps {
    title?: string;
    children: ReactNode;
    className?: string;
}

export default function Section({ title, children, className = "" }: SectionProps) {
    return (
        <section className={`mb-4 sm:mb-8 px-0 sm:px-6 ${className}`}>
            <div className="bg-white border border-border rounded-lg p-4 sm:p-6 shadow-sm">
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