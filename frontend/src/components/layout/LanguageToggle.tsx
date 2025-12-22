import { useTranslation } from "react-i18next";

export default function LanguageToggle() {
    const { i18n } = useTranslation();
    const current = i18n.language;

    function setLang(lang: "sv" | "en") {
        if (lang === current) return;
        i18n.changeLanguage(lang);
        localStorage.setItem("language", lang);
    }

    const base =
        "text-sm font-medium transition-colors";
    const active =
        "text-primary border-b-2 border-primary";
    const inactive =
        "text-gray-500 hover:text-gray-800";

    return (
        <div className="flex items-center gap-2">
            <button
                aria-current={current === "sv"}
                onClick={() => setLang("sv")}
                className={`${base} ${current === "sv" ? active : inactive}`}
            >
                SV
            </button>

            <span className="text-gray-400">|</span>

            <button
                aria-current={current === "en"}
                onClick={() => setLang("en")}
                className={`${base} ${current === "en" ? active : inactive}`}
            >
                EN
            </button>
        </div>
    );
}