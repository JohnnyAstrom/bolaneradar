import { useTranslation } from "react-i18next";

export default function Footer() {
    const { t } = useTranslation();
    const year = new Date().getFullYear();

    return (
        <footer className="bg-bg-light border-t border-border mt-12">
            <div className="max-w-6xl mx-auto px-4 py-6 text-center text-text-secondary text-sm">
                {t("footer.copyright", { year })}
            </div>
        </footer>
    );
}