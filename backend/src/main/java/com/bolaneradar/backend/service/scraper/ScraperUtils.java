package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.MortgageTerm;
import java.math.BigDecimal;

public class ScraperUtils {

    /**
     * Försöker tolka text som "3 mån", "1 år" etc. till motsvarande MortgageTerm.
     */
    public static MortgageTerm parseTerm(String text) {
        if (text == null) return null;
        text = text.toLowerCase().trim();

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

        // Engelska versioner (för banker som Hypoteket)
        if (text.contains("three month")) return MortgageTerm.VARIABLE_3M;
        if (text.contains("one year")) return MortgageTerm.FIXED_1Y;
        if (text.contains("two year")) return MortgageTerm.FIXED_2Y;
        if (text.contains("three year")) return MortgageTerm.FIXED_3Y;
        if (text.contains("four year")) return MortgageTerm.FIXED_4Y;
        if (text.contains("five year")) return MortgageTerm.FIXED_5Y;
        if (text.contains("seven year")) return MortgageTerm.FIXED_7Y;
        if (text.contains("ten year")) return MortgageTerm.FIXED_10Y;

        return null;
    }

    /**
     * Tar bort procenttecken, kommatecken och whitespace.
     * Returnerar BigDecimal eller null om det inte går att tolka.
     */
    public static BigDecimal parseRate(String text) {
        if (text == null) return null;

        text = text
                .replace("%", "")
                .replace(",", ".")
                .replace("−", "-")  // fångar upp icke-standard minus-tecken
                .trim()
                .toLowerCase();

        // Fångar upp vanliga icke-numeriska värden
        if (text.isEmpty() ||
                text.equals("n/a") ||
                text.equals("na") ||
                text.equals("-") ||
                text.equals("–") ||  // långt streck
                text.equals("–") ||
                text.contains("ej") ||
                text.contains("inte") ||
                text.contains("nan")) {
            return null;
        }

        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            System.err.println("Ogiltig ränta: '" + text + "'");
            return null;
        }
    }
}
