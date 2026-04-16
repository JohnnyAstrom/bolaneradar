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
            ? "text-primary bg-primary/8"
            : "text-slate-700 hover:text-primary hover:bg-slate-50";

    return (
        <header className="sticky top-0 z-30 w-full bg-white/92 backdrop-blur border-b border-slate-200/80 px-4 py-3">
            <div className="max-w-5xl mx-auto flex items-center justify-between gap-4">

                <NavLink
                    to="/"
                    end
                    className="text-xl font-semibold text-primary flex items-center gap-2"
                >
                    <img
                        src="/android-chrome-192x192.png"
                        alt=""
                        className="w-6 h-6"
                        aria-hidden
                    />
                    BolåneRadar
                </NavLink>

                <nav className="hidden md:flex items-center gap-2 rounded-full border border-slate-200/80 bg-white/90 px-2 py-2 shadow-sm">
                    <NavLink to="/" end className={({ isActive }) => `rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${linkClass({ isActive })}`}>
                        {t("nav.home")}
                    </NavLink>

                    <NavLink to="/bolaneguide" className={({ isActive }) => `rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${linkClass({ isActive })}`}>
                        {t("nav.guide")}
                    </NavLink>

                    <NavLink to="/rate-updates" className={({ isActive }) => `rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${linkClass({ isActive })}`}>
                        {t("nav.rateUpdates")}
                    </NavLink>

                    <NavLink to="/om-bolaneradar" className={({ isActive }) => `rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${linkClass({ isActive })}`}>
                        {t("nav.about")}
                    </NavLink>

                    <div className="ml-2 pl-3 border-l border-slate-200">
                        <LanguageToggle />
                    </div>
                </nav>

                <button
                    className="md:hidden p-2 rounded-xl hover:bg-gray-100 transition-colors"
                    onClick={() => setOpen(true)}
                >
                    <Menu size={24} />
                </button>
            </div>

            <aside
                className={`
                    fixed inset-0 z-50 bg-white shadow-xl
                    transform transition-transform duration-300 md:hidden
                    ${open ? "translate-x-0" : "translate-x-full"}
                `}
                aria-hidden={!open}
            >
                <div className="flex h-full flex-col bg-white">
                <div className="p-4 flex items-center justify-between border-b border-slate-200 bg-white">
                    <span className="font-semibold text-lg">
                        {t("nav.menu")}
                    </span>
                    <button
                        className="p-2 rounded-xl hover:bg-gray-100"
                        onClick={() => setOpen(false)}
                    >
                        <X size={26} />
                    </button>
                </div>

                <nav className="flex flex-1 flex-col p-4 gap-2 bg-white text-gray-800">
                    <NavLink
                        to="/"
                        end
                        onClick={() => setOpen(false)}
                        className={({ isActive }) => `rounded-xl px-3 py-2 text-sm font-medium transition-colors ${linkClass({ isActive })}`}
                    >
                        {t("nav.home")}
                    </NavLink>

                    <NavLink
                        to="/bolaneguide"
                        onClick={() => setOpen(false)}
                        className={({ isActive }) => `rounded-xl px-3 py-2 text-sm font-medium transition-colors ${linkClass({ isActive })}`}
                    >
                        {t("nav.guide")}
                    </NavLink>

                    <NavLink
                        to="/rate-updates"
                        onClick={() => setOpen(false)}
                        className={({ isActive }) => `rounded-xl px-3 py-2 text-sm font-medium transition-colors ${linkClass({ isActive })}`}
                    >
                        {t("nav.rateUpdates")}
                    </NavLink>

                    <NavLink
                        to="/om-bolaneradar"
                        onClick={() => setOpen(false)}
                        className={({ isActive }) => `rounded-xl px-3 py-2 text-sm font-medium transition-colors ${linkClass({ isActive })}`}
                    >
                        {t("nav.about")}
                    </NavLink>

                    <div className="mt-4 border-t border-slate-200 pt-4">
                        <LanguageToggle />
                    </div>
                </nav>
                </div>
            </aside>
        </header>
    );
}
