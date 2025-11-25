import type { ReactNode } from "react";

interface SectionProps {
    title?: string;
    children: ReactNode;
}

export default function Section({ title, children }: SectionProps) {
    return (
        <section className="bg-white rounded-lg shadow-sm p-6 mb-8">
            {title && (
                <h2 className="text-xl font-semibold text-text-primary mb-4">
                    {title}
                </h2>
            )}
            {children}
        </section>
    );
}