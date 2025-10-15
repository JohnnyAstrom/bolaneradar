package com.bolaneradar.backend.model;

/**
 * Enum som listar de bindningstider vi vill stödja.
 * Bara dessa värden är giltiga i databasen.
 */
public enum MortgageTerm {
    VARIABLE_3M,   // rörlig ränta (3 månader)
    FIXED_1Y,      // bunden 1 år
    FIXED_2Y,      // bunden 2 år
    FIXED_3Y,      // bunden 3 år
    FIXED_5Y       // bunden 5 år
}