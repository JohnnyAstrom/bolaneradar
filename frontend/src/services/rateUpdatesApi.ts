import type { RateUpdateDay } from "../types/rateUpdates";
import client from "./client";

export async function fetchRateUpdates(): Promise<RateUpdateDay[]> {
    const response = await client.get("/api/rates/updates");
    return response.data;
}