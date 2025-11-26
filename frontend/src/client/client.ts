import axios from "axios";

export const API_URL = "http://localhost:8080";

const client = axios.create({
    baseURL: API_URL,
    timeout: 10000,          // skyddar mot hängningar
    withCredentials: false,  // din backend kräver inte cookies
});

// Litet helper som mappar .data direkt
export async function apiGet<T>(path: string): Promise<T> {
    const response = await client.get<T>(path);
    return response.data;
}

export default client;