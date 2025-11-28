import type { ReactNode } from "react";

interface PageWrapperProps {
    children: ReactNode;
}

export default function PageWrapper({ children }: PageWrapperProps) {
    return (
        <div
            className="
                max-w-6xl
                mx-auto
                px-2      /* Mobil */
                sm:px-4   /* Tablet */
                lg:px-6   /* Desktop */
                py-6
                md:py-8
            "
        >
            {children}
        </div>
    );
}