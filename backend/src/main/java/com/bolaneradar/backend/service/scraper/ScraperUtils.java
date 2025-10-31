package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.MortgageTerm;

public class ScraperUtils {

    public static MortgageTerm parseTerm(String text) {
        text = text.toLowerCase();

        // Svenska versioner
        if (text.contains("3 mån") || text.contains("rörlig")) return MortgageTerm.VARIABLE_3M;
        if (text.contains("1 år")) return MortgageTerm.FIXED_1Y;
        if (text.contains("2 år")) return MortgageTerm.FIXED_2Y;
        if (text.contains("3 år")) return MortgageTerm.FIXED_3Y;
        if (text.contains("4 år")) return MortgageTerm.FIXED_4Y;
        if (text.contains("5 år")) return MortgageTerm.FIXED_5Y;
        if (text.contains("6 år")) return MortgageTerm.FIXED_6Y;
        if (text.contains("7 år")) return MortgageTerm.FIXED_7Y;
        if (text.contains("8 år")) return MortgageTerm.FIXED_8Y;
        if (text.contains("10 år")) return MortgageTerm.FIXED_10Y;

        // Engelska varianter (Hypoteket m.fl.)
        if (text.contains("threemonth")) return MortgageTerm.VARIABLE_3M;
        if (text.contains("oneyear")) return MortgageTerm.FIXED_1Y;
        if (text.contains("twoyear")) return MortgageTerm.FIXED_2Y;
        if (text.contains("threeyear")) return MortgageTerm.FIXED_3Y;
        if (text.contains("fouryear")) return MortgageTerm.FIXED_4Y;
        if (text.contains("fiveyear")) return MortgageTerm.FIXED_5Y;
        if (text.contains("sevenyear")) return MortgageTerm.FIXED_7Y;
        if (text.contains("tenyear")) return MortgageTerm.FIXED_10Y;

        return null;
    }
}