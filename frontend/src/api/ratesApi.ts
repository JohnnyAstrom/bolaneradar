import { apiGet } from "./client";
import type {MortgageRate} from "../types/mortgage";

export async function getAllRates(): Promise<MortgageRate[]> {
    return apiGet("/api/rates");
}