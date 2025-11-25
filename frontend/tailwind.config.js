/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // --- Primary Brand Colors ---
                primary: {
                    DEFAULT: "#1E88E5",     // Primary Blue
                    hover: "#1565C0",       // Hover Blue
                    active: "#0D47A1",      // Active Blue
                    light: "#90CAF9",       // Soft highlight / focus outline
                },

                // --- Backgrounds ---
                bg: {
                    light: "#F5F7FA",       // Section background
                    white: "#FFFFFF",
                },

                // --- Text Colors ---
                text: {
                    primary: "#1A1A1A",     // Headlines + important text
                    secondary: "#5A5A5A",   // Subtle text
                },

                // --- Borders & UI Lines ---
                border: {
                    DEFAULT: "#D0D4DA",     // Default border
                    hover: "#A0A4A8",       // Input-border hover
                    focus: "#1E88E5",       // 2px focus color
                },

                // --- Table Row Colors ---
                row: {
                    DEFAULT: "#FFFFFF",
                    hover: "#F0F4F8",
                    active: "#E3F2FD",
                    focus: "#E3F2FD",
                },

                // --- Status Colors ---
                positive: "#43A047",      // Rate decrease
                negative: "#E53935",      // Rate increase

                // --- Icon Colors ---
                icon: {
                    neutral: "#A0A4A8",     // Sort arrow default
                },

                // --- Link Colors ---
                link: {
                    DEFAULT: "#1E88E5",
                    hover: "#1565C0",
                    active: "#0D47A1",
                },
            },

            // Optional: Standard container setup
            container: {
                center: true,
                padding: "1rem",
                screens: {
                    sm: "640px",
                    md: "768px",
                    lg: "1024px",
                    xl: "1280px",
                },
            },
        },
    },
    plugins: [],
};