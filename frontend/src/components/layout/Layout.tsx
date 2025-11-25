import { Outlet } from "react-router-dom";
import Header from "./Header";
import Footer from "./Footer";

export default function Layout() {
    return (
        <div className="min-h-screen flex flex-col bg-bg-light">

            {/* Top navigation */}
            <Header />

            {/* Main content (pages render here) */}
            <main className="flex-1">
                <Outlet />
            </main>

            {/* Footer */}
            <Footer />
        </div>
    );
}