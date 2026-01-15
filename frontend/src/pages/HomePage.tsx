import { useState, useRef } from "react";

import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

import Hero from "../components/home/Hero";
import SmartRateTestForm from "../components/home/SmartRateTestForm";

import RatesHeader from "../components/home/RatesHeader";
import TermSelector from "../components/home/TermSelector";
import ComparisonTable from "../components/home/ComparisonTable";
import TableFooter from "../components/home/TableFooter";

export default function HomePage() {
    const [smartTestActive, setSmartTestActive] = useState(false);
    const [activeTerm, setActiveTerm] = useState<string>("3m");
    const ratesRef = useRef<HTMLDivElement | null>(null);

    function scrollToRates() {
        ratesRef.current?.scrollIntoView({
            behavior: "smooth",
            block: "start",
        });
    }

    return (
        <PageWrapper>

            {/* Hero */}
            <Hero
                smartTestActive={smartTestActive}
                onToggleTest={() => setSmartTestActive(!smartTestActive)}
            />

            {/* SmartTest block - shown only when active */}
            {smartTestActive && (
                <>
                    <Section>
                        <SmartRateTestForm onScrollToRates={scrollToRates} />
                    </Section>
                </>
            )}

            {/* Rate table section */}
            <Section id="rates">
                <RatesHeader />

                <TermSelector
                    activeTerm={activeTerm}
                    onSelectTerm={setActiveTerm}
                />

                <div ref={ratesRef} className="scroll-mt-24">
                    <ComparisonTable activeTerm={activeTerm} />
                </div>

                <TableFooter />
            </Section>

        </PageWrapper>
    );
}