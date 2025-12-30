import { useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/layout/Layout";

import HomePage from "./pages/HomePage";
import GuidePage from "./pages/GuidePage";
import AboutPage from "./pages/AboutPage";
import ContactPage from "./pages/ContactPage";
import BankPage from "./pages/BankPage";
import BankInfoPage from "./pages/BankInfoPage";

export default function App() {

    useEffect(() => {
        // Warmup: väck backend direkt
        fetch(`${import.meta.env.VITE_API_URL}/api/health`)
            .catch(() => {
                // Ignorera – detta är bara warmup
            });
    }, []);

    return (
        <BrowserRouter>
            <Routes>
                <Route element={<Layout />}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/guide" element={<GuidePage />} />
                    <Route path="/om-oss" element={<AboutPage />} />
                    <Route path="/contact" element={<ContactPage />} />
                    <Route path="/bank/:bankKey" element={<BankPage />} />
                    <Route path="/bank/:bankKey/info" element={<BankInfoPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}