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
        // Warmup: väck backend direkt (Render kan vara seg vid cold start)
        // AbortController gör att vi inte lämnar hängande requests
        // om komponenten unmountas (t.ex. vid snabb reload).
        const controller = new AbortController();

        fetch(`${import.meta.env.VITE_API_URL}/api/health`, {
            signal: controller.signal,
        }).catch(() => {
            // Ignorera fel – detta är bara ett warmup-anrop
            // Backend kan vara nere eller långsam utan att det är ett problem här
        });

        // Städa upp requesten om komponenten försvinner
        return () => {
            controller.abort();
        };
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