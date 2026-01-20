package com.bolaneradar.backend.entity.enums.smartrate;

/**
 * Jämförelseresultat mellan två räntor,
 * t.ex. kundens ränta kontra marknadens.
 */

public enum RateComparison {
    HIGHER,
    LOWER,
    EQUAL,
    UNKNOWN
}