import { Link } from "react-router-dom";

export default function Navbar() {
    return (
        <nav className="bg-blue-600 text-white p-4">
            <ul className="flex gap-6">
                <li><Link to="/">Hem</Link></li>
                <li><Link to="/rates">Räntor</Link></li>
                <li><Link to="/history">Historik</Link></li>
                <li><Link to="/analytics">Analytics</Link></li>
            </ul>
        </nav>
    );
}