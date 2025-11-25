export default function Footer() {
    return (
        <footer className="bg-bg-light border-t border-border mt-12">
            <div className="max-w-6xl mx-auto px-4 py-6 text-center text-text-secondary text-sm">
                © {new Date().getFullYear()} BolåneRadar — All rights reserved.
            </div>
        </footer>
    );
}