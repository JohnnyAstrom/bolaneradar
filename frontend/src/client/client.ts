import axios from "axios";

export const API_URL = import.meta.env.VITE_API_URL;

const client = axios.create({
    baseURL: API_URL,
    timeout: 30000, // 30 sek
    withCredentials: false,
});

// GET helper
export async function apiGet<T>(path: string): Promise<T> {
    const response = await client.get<T>(path);
    return response.data;
}

// POST helper – helt typad payload, ingen "any"
export async function apiPost<TResponse, TPayload>(
    path: string,
    payload: TPayload
): Promise<TResponse> {
    const response = await client.post<TResponse>(path, payload);
    return response.data;
}

export default client;