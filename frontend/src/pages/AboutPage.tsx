import PageWrapper from "../components/layout/PageWrapper";
import Section from "../components/layout/Section";

export default function AboutPage() {
    return (
        <PageWrapper>
            <Section title="Om BolåneRadar">
                <p className="text-text-secondary">
                    Här kommer information om BolåneRadar.
                </p>
            </Section>
        </PageWrapper>
    );
}