import type { ReactNode } from "react";

interface SectionProps {
    title?: string;
    children: ReactNode;
}

export default function Section({ title, children }: SectionProps) {
    return (
        <section
            className="
                mb-6
                px-0
                bg-transparent
                shadow-none
                rounded-none
                sm:bg-white sm:rounded-lg sm:shadow-sm sm:p-6
            "
        >
            {title && (
                <h2 className="text-xl font-semibold text-text-primary mb-4">
                    {title}
                </h2>
            )}
            {children}
        </section>
    );
}
