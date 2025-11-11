package com.bolaneradar.backend.entity.enums;

/**
 * Enum som listar de bindningstider vi vill stödja.
 * Bara dessa värden är giltiga i databasen.
 */
public enum MortgageTerm {
    VARIABLE_3M,   // rörlig ränta (3 månader)
    FIXED_1Y,      // bunden 1 år
    FIXED_2Y,      // bunden 2 år
    FIXED_3Y,      // bunden 3 år
    FIXED_4Y,      // bunden 4 år
    FIXED_5Y,       // bunden 5 år
    FIXED_6Y,       // bunden 6 år
    FIXED_7Y,       // bunden 7 år
    FIXED_8Y,       // bunden 8 år
    FIXED_9Y,
    FIXED_10Y       // bunden 10 år
}