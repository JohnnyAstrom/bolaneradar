import { bankKeyMap } from "./bankKeyMap";

/**
 * bankNameMap
 *
 * Konverterar bankKeyMap (DB-namn → urlKey)
 * till en omvänd mapping (urlKey → DB-namn).
 *
 * Exempel:
 *    bankKeyMap = { "Swedbank": "swedbank" }
 *    bankNameMap = { "swedbank": "Swedbank" }
 */
export const bankNameMap: Record<string, string> = Object.fromEntries(
    Object.entries(bankKeyMap).map(([dbName, urlKey]) => [
        urlKey.toLowerCase(),
        dbName
    ])
);