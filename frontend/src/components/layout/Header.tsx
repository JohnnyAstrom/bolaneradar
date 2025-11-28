import { useState } from "react";
import { Link } from "react-router-dom";
import { Menu, X } from "lucide-react"; // ikonbibliotek, superlätta

export default function Header() {
    const [open, setOpen] = useState(false);

    return (
        <header className="w-full bg-white border-b border-gray-200 px-4 py-3">
            <div className="max-w-5xl mx-auto flex items-center justify-between">

                {/* LOGO */}
                <Link to="/" className="text-xl font-bold text-primary flex items-center">
                    BolåneRadar
                </Link>

                {/* DESKTOP MENY */}
                <nav className="hidden md:flex gap-6 text-gray-700">
                    <Link to="/">Start</Link>
                    <Link to="/guide">Guide</Link>
                    <Link to="/om">Om</Link>
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
                <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
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
                    <span className="font-semibold text-lg">Meny</span>
                    <button
                        className="p-2 rounded hover:bg-gray-100"
                        onClick={() => setOpen(false)}
                    >
                        <X size={26} />
                    </button>
                </div>

                <nav className="flex flex-col p-4 gap-4 text-gray-800">
                    <Link to="/" onClick={() => setOpen(false)}>Start</Link>
                    <Link to="/guide" onClick={() => setOpen(false)}>Guide</Link>
                    <Link to="/om" onClick={() => setOpen(false)}>Om</Link>
                </nav>
            </aside>
        </header>
    );
}