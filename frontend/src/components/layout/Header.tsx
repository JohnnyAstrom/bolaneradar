import { NavLink } from "react-router-dom";

interface NavItem {
    label: string;
    path: string;
}

const navItems: NavItem[] = [
    { label: "Start", path: "/" },
    { label: "Guide", path: "/guide" },
    { label: "Om", path: "/about" },
    { label: "Kontakt", path: "/contact" },
];

export default function Header() {
    return (
        <header className="bg-white border-b border-border">
            <nav className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">

                {/* Logo */}
                <div className="text-2xl font-bold text-primary">
                    BolåneRadar
                </div>

                {/* Navigation Items */}
                <ul className="flex gap-8 text-text-secondary">
                    {navItems.map((item) => (
                        <li key={item.path}>
                            <NavLink
                                to={item.path}
                                className={({ isActive }) =>
                                    `transition-colors ${
                                        isActive
                                            ? "text-primary font-semibold underline underline-offset-4"
                                            : "hover:text-primary"
                                    }`
                                }
                            >
                                {item.label}
                            </NavLink>
                        </li>
                    ))}
                </ul>

            </nav>
        </header>
    );
};