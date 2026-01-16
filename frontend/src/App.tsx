import { useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/layout/Layout";
import ScrollToTop from "./components/routing/ScrollToTop.tsx";

import HomePage from "./pages/HomePage";
import BolaneguidePage from "./pages/BolaneguidePage.tsx";
import AboutBolaneRadarPage from "./pages/AboutBolaneRadarPage.tsx";
import BankPage from "./pages/BankPage";
import BankInfoPage from "./pages/BankInfoPage";
import RateUpdatesPage from "./pages/RateUpdatesPage";

export default function App() {

    // Recharts kan logga en falsk varning om width/height = -1
    // vid initial mount trots korrekt layout.
    // Filtreras bort globalt eftersom den saknar signalvärde.
    useEffect(() => {
        const originalWarn = console.warn;

        console.warn = (...args: unknown[]) => {
            if (
                typeof args[0] === "string" &&
                args[0].includes("The width(-1) and height(-1) of chart")
            ) {
                return;
            }

            originalWarn(...args);
        };
    }, []);

    /* ============================================================
     * WARMUP AV BACKEND (RENDER COLD START)
     * ============================================================ */
    useEffect(() => {
        const controller = new AbortController();

        fetch(`${import.meta.env.VITE_API_URL}/api/health`, {
            signal: controller.signal,
        }).catch(() => {
            // Ignorera fel – detta är bara ett warmup-anrop
        });

        return () => {
            controller.abort();
        };
    }, []);

    return (
        <BrowserRouter>
            <ScrollToTop />
            <Routes>
                <Route element={<Layout />}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/bolaneguide" element={<BolaneguidePage />} />
                    <Route path="/om-bolaneradar" element={<AboutBolaneRadarPage />} />
                    <Route path="/bank/:bankKey" element={<BankPage />} />
                    <Route path="/bank/:bankKey/info" element={<BankInfoPage />} />
                    <Route path="/rate-updates" element={<RateUpdatesPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}