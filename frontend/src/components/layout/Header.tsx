import { useState } from "react";
import { NavLink } from "react-router-dom";
import { Menu, X } from "lucide-react";
import { useTranslation } from "react-i18next";
import LanguageToggle from "./LanguageToggle";

export default function Header() {
    const [open, setOpen] = useState(false);
    const { t } = useTranslation();

    const linkClass = ({ isActive }: { isActive: boolean }) =>
        isActive
            ? "text-primary underline underline-offset-4"
            : "hover:underline hover:underline-offset-4";

    return (
        <header className="w-full bg-white border-b border-gray-200 px-4 py-3">
            <div className="max-w-5xl mx-auto flex items-center justify-between">

                {/* LOGO */}
                <NavLink
                    to="/"
                    end
                    className="text-xl font-bold text-primary flex items-center"
                >
                    BolåneRadar
                </NavLink>

                {/* DESKTOP MENY */}
                <nav className="hidden md:flex gap-6 text-gray-700 items-center">
                    <NavLink to="/" end className={linkClass}>
                        {t("nav.home")}
                    </NavLink>
                    <NavLink to="/guide" className={linkClass}>
                        {t("nav.guide")}
                    </NavLink>
                    <NavLink to="/om-oss" className={linkClass}>
                        {t("nav.about")}
                    </NavLink>

                    {/* SPRÅK */}
                    <LanguageToggle />
                </nav>

                {/* MOBIL KNAPP */}
                <button
                    className="md:hidden p-2 rounded hover:bg-gray-100"
                    onClick={() => setOpen(true)}
                >
                    <Menu size={24} />
                </button>
            </div>

            {/* MOBIL MENY OVERLAY */}
            {open && (
                <div
                    className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
                    onClick={() => setOpen(false)}
                />
            )}

            {/* MOBIL MENY PANEL */}
            <aside
                className={`
                    fixed top-0 right-0 h-full w-64 bg-white shadow-lg z-50
                    transform transition-transform duration-300
                    ${open ? "translate-x-0" : "translate-x-full"}
                `}
            >
                <div className="p-4 flex items-center justify-between border-b border-gray-200">
                    <span className="font-semibold text-lg">
                        {t("nav.menu")}
                    </span>
                    <button
                        className="p-2 rounded hover:bg-gray-100"
                        onClick={() => setOpen(false)}
                    >
                        <X size={26} />
                    </button>
                </div>

                <nav className="flex flex-col p-4 gap-4 text-gray-800">
                    <NavLink
                        to="/"
                        end
                        onClick={() => setOpen(false)}
                        className={linkClass}
                    >
                        {t("nav.home")}
                    </NavLink>
                    <NavLink
                        to="/guide"
                        onClick={() => setOpen(false)}
                        className={linkClass}
                    >
                        {t("nav.guide")}
                    </NavLink>
                    <NavLink
                        to="/om-oss"
                        onClick={() => setOpen(false)}
                        className={linkClass}
                    >
                        {t("nav.about")}
                    </NavLink>

                    {/* SPRÅK – MOBIL */}
                    <div className="mt-6">
                        <LanguageToggle />
                    </div>
                </nav>
            </aside>
        </header>
    );
}