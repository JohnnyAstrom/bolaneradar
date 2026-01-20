package com.bolaneradar.backend.entity.enums.smartrate;

/**
 * Klassificering av hur bra eller dålig en ränta är
 * jämfört med marknaden i Smart Räntetestet.
 */
public enum SmartRateStatus {
    GREAT_GREEN,
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    INFO,
    UNKNOWN
}