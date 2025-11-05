package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.MortgageTerm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScraperUtils {

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("januari", 1), Map.entry("februari", 2),
            Map.entry("mars", 3), Map.entry("april", 4),
            Map.entry("maj", 5), Map.entry("juni", 6),
            Map.entry("juli", 7), Map.entry("augusti", 8),
            Map.entry("september", 9), Map.entry("oktober", 10),
            Map.entry("november", 11), Map.entry("december", 12)
    );

    /** Standardiserad Jsoup-hämtning med user-agent och timeout */
    public static Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();
    }

    /** Försöker tolka text som "3 mån", "1 år" etc. till motsvarande term */
    public static MortgageTerm parseTerm(String text) {
        if (text == null) return null;
        text = text.toLowerCase(Locale.ROOT).trim();

        if (text.contains("3 mån") || text.contains("rörlig")) return MortgageTerm.VARIABLE_3M;
        if (text.contains("1 år")) return MortgageTerm.FIXED_1Y;
        if (text.contains("2 år")) return MortgageTerm.FIXED_2Y;
        if (text.contains("3 år")) return MortgageTerm.FIXED_3Y;
        if (text.contains("4 år")) return MortgageTerm.FIXED_4Y;
        if (text.contains("5 år")) return MortgageTerm.FIXED_5Y;
        if (text.contains("6 år")) return MortgageTerm.FIXED_6Y;
        if (text.contains("7 år")) return MortgageTerm.FIXED_7Y;
        if (text.contains("8 år")) return MortgageTerm.FIXED_8Y;
        if (text.contains("9 år")) return MortgageTerm.FIXED_9Y;
        if (text.contains("10 år")) return MortgageTerm.FIXED_10Y;

        // Engelska versioner
        if (text.contains("three month")) return MortgageTerm.VARIABLE_3M;
        if (text.contains("one year")) return MortgageTerm.FIXED_1Y;
        if (text.contains("two year")) return MortgageTerm.FIXED_2Y;
        if (text.contains("three year")) return MortgageTerm.FIXED_3Y;
        if (text.contains("four year")) return MortgageTerm.FIXED_4Y;
        if (text.contains("five year")) return MortgageTerm.FIXED_5Y;
        if (text.contains("six year")) return MortgageTerm.FIXED_6Y;
        if (text.contains("seven year")) return MortgageTerm.FIXED_7Y;
        if (text.contains("eight year")) return MortgageTerm.FIXED_8Y;
        if (text.contains("nine year")) return MortgageTerm.FIXED_9Y;
        if (text.contains("ten year")) return MortgageTerm.FIXED_10Y;

        return null;
    }

    /** Tar bort %, byter , till ., hanterar minus och whitespace */
    public static BigDecimal parseRate(String text) {
        if (text == null) return null;

        text = text.replace("%", "")
                .replace(",", ".")
                .replace("−", "-")
                .trim()
                .toLowerCase(Locale.ROOT);

        if (text.isEmpty() || text.matches("^(n/a|na|-|–|ej|inte|nan)$")) return null;

        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            System.err.println("Ogiltig ränta: '" + text + "'");
            return null;
        }
    }

    /** Tolkar svensk månad + år ur text (ex. "september 2025") */
    public static YearMonth parseSwedishMonth(String text) {
        if (text == null) return YearMonth.from(LocalDate.now());
        String lower = text.toLowerCase(Locale.ROOT);
        for (String key : MONTHS.keySet()) {
            if (lower.contains(key)) {
                Matcher matcher = Pattern.compile("(20\\d{2})").matcher(lower);
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group(1));
                    return YearMonth.of(year, MONTHS.get(key));
                }
            }
        }
        return YearMonth.from(LocalDate.now());
    }

    /** Enklare loggfunktion för konsolutskrifter */
    public static void logResult(String bankName, int count) {
        System.out.println(bankName + ": hittade " + count + " räntor totalt.");
    }
}