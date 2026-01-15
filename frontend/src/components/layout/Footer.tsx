import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

export default function Footer() {
    const { t } = useTranslation();
    const year = new Date().getFullYear();

    return (
        <footer className="bg-bg-light border-t border-border mt-12">
            <div className="max-w-6xl mx-auto px-4 py-6 text-center text-text-secondary text-sm space-y-2">

                <div>
                    {t("footer.description")}
                </div>

                <div className="flex justify-center gap-4">
                    <a
                        href="https://github.com/JohnnyAstrom/bolaneradar"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="hover:underline"
                    >
                        GitHub
                    </a>

                    <Link to="/om-bolaneradar" className="hover:underline">
                        {t("footer.about")}
                    </Link>
                </div>

                <div>
                    {t("footer.copyright", { year })}
                </div>
            </div>
        </footer>
    );
}