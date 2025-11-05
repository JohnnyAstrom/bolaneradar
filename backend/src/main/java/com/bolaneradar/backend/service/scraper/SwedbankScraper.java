package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.*;

/**
 * Webbskrapare för Swedbank.
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor) bolåneräntor.
 */
@Service
public class SwedbankScraper implements BankScraper {

    private static final String URL = "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor.html";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();

        // 🔹 Försök hitta texten som anger vilken månad snitträntan gäller för
        // Exempel: "Genomsnittsränta, september 2025"
        String monthText = doc.text();
        YearMonth averageMonth = parseReportedMonth(monthText);
        LocalDate averageEffectiveDate = averageMonth.atDay(1);

        Elements tables = doc.select("table");
        System.out.println("🔍 Antal tabeller hittade: " + tables.size());

        for (Element table : tables) {
            String caption = "";
            Element capEl = table.selectFirst("caption");
            if (capEl != null) caption = capEl.text().toLowerCase();

            String heading = "";
            Element prev = table.previousElementSibling();
            int checkDepth = 0;
            while (prev != null && checkDepth < 5) {
                if (prev.tagName().matches("h2|h3|h4|strong|p")) {
                    heading = prev.text().toLowerCase();
                    break;
                }
                prev = prev.previousElementSibling();
                checkDepth++;
            }

            String context = caption + " " + heading;

            RateType rateType;
            if (context.contains("genomsnitt")) {
                rateType = RateType.AVERAGERATE;
            } else if (context.contains("aktuella") || context.contains("list")) {
                rateType = RateType.LISTRATE;
            } else {
                continue;
            }

            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                String termText = cols.get(0).text().toLowerCase();
                String rateText = cols.get(1).text()
                        .replace("%", "")
                        .replace(",", ".")
                        .trim();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                if (term != null && !rateText.isEmpty()) {
                    try {
                        BigDecimal rate = new BigDecimal(rateText);

                        // 💡 Här skiljer vi logiken för LISTRATE och AVERAGERATE
                        LocalDate effectiveDate = (rateType == RateType.AVERAGERATE)
                                ? averageEffectiveDate
                                : LocalDate.now();

                        rates.add(new MortgageRate(bank, term, rateType, rate, effectiveDate));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        System.out.println("Swedbank: hittade " + rates.size() + " räntor totalt.");
        return rates;
    }

    /**
     * Tolkar text som "Genomsnittsränta, september 2025" → YearMonth(2025, 9)
     */
    private YearMonth parseReportedMonth(String text) {
        Map<String, Integer> months = Map.ofEntries(
                Map.entry("januari", 1), Map.entry("februari", 2),
                Map.entry("mars", 3), Map.entry("april", 4),
                Map.entry("maj", 5), Map.entry("juni", 6),
                Map.entry("juli", 7), Map.entry("augusti", 8),
                Map.entry("september", 9), Map.entry("oktober", 10),
                Map.entry("november", 11), Map.entry("december", 12)
        );

        String lower = text.toLowerCase(Locale.ROOT);
        for (String key : months.keySet()) {
            if (lower.contains(key)) {
                Matcher matcher = Pattern.compile("(20\\d{2})").matcher(lower);
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group(1));
                    return YearMonth.of(year, months.get(key));
                }
            }
        }

        // Om inget hittas, fallback till nuvarande månad
        System.out.println("⚠️ Kunde inte tolka månad för snittränta, använder nuvarande månad.");
        return YearMonth.from(LocalDate.now());
    }
}