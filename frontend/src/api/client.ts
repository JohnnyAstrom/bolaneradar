export const API_URL = "http://localhost:8080";

export async function apiGet(path: string) {
    const res = await fetch(`${API_URL}${path}`);
    if (!res.ok) throw new Error("API error: " + res.status);
    return res.json();
}
