import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/layout/Layout";

import HomePage from "./pages/HomePage";
import GuidePage from "./pages/GuidePage.tsx";
import AboutPage from "./pages/AboutPage.tsx";
import ContactPage from "./pages/ContactPage.tsx";
import BankPage from "./pages/BankPage";


export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Alla routes som ska ha Header + Footer ligger här */}
                <Route element={<Layout />}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/guide" element={<GuidePage />} />
                    <Route path="/about" element={<AboutPage />} />
                    <Route path="/contact" element={<ContactPage />} />
                    <Route path="/bank/:bankName" element={<BankPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}