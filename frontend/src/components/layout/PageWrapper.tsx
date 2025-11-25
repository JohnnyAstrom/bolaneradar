import type { ReactNode } from "react";

interface PageWrapperProps {
    children: ReactNode;
}

export default function PageWrapper({ children }: PageWrapperProps) {
    return (
        <div className="max-w-6xl mx-auto px-4 py-8">
            {children}
        </div>
    );
}