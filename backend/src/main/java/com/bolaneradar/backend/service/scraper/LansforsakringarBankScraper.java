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
import java.util.ArrayList;
import java.util.List;

/**
 * Webbskrapare för Länsförsäkringar.
 * Hämtar både snitträntor och listräntor från den regionala webbsidan.
 * För snitträntor används datum från text som "Genomsnittliga bolåneräntor för september 2025".
 */
@Service
public class LansforsakringarBankScraper implements BankScraper {

    private static final String URL =
            "https://www.lansforsakringar.se/norrbotten/privat/bank/bolan/bolaneranta/";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();

        // Försök hitta texten "Genomsnittliga bolåneräntor för september 2025"
        LocalDate averageDate = extractAverageDate(doc);
        if (averageDate != null) {
            System.out.println("Länsförsäkringar: snitträntor gäller " + averageDate);
        }

        Elements tables = doc.select("table.lf-table");
        System.out.println("Länsförsäkringar: hittade " + tables.size() + " tabeller.");

        for (int i = 0; i < tables.size(); i++) {
            Element table = tables.get(i);
            RateType rateType = (i == 0) ? RateType.AVERAGERATE : RateType.LISTRATE;
            LocalDate date = (rateType == RateType.AVERAGERATE && averageDate != null)
                    ? averageDate
                    : LocalDate.now();

            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                String termText = cols.get(0).text().toLowerCase();
                String rateText = cols.get(1).text()
                        .replace("%", "")
                        .replace(",", ".")
                        .trim();

                if (rateText.isEmpty() || rateText.equals("-") || rateText.equals("&nbsp;")) continue;

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                if (term == null) continue;

                try {
                    BigDecimal rate = new BigDecimal(rateText);
                    rates.add(new MortgageRate(bank, term, rateType, rate, date));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (rates.isEmpty()) {
            System.out.println("Länsförsäkringar: inga räntor hittades.");
        } else {
            System.out.println("Länsförsäkringar: hittade " + rates.size() + " räntor totalt.");
            long listCount = rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).count();
            long avgCount = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).count();
            System.out.println(" - Listräntor: " + listCount);
            System.out.println(" - Snitträntor: " + avgCount);
        }

        return rates;
    }

    /** Försöker hitta text typ "Genomsnittlig ränta september 2025" eller "Genomsnittliga bolåneräntor för september 2025" */
    private LocalDate extractAverageDate(Document doc) {
        try {
            // Matchar rubriker eller stycken som innehåller "genomsnittlig" och "ränta"
            Element heading = doc.selectFirst(":matchesOwn((?i)genomsnittlig[a]?[a-z\\s]*ränta)");
            if (heading == null) return null;

            String text = heading.text().toLowerCase();

            // Plocka ut månad + år oavsett om "för" finns eller inte
            // Ex: "genomsnittlig ränta september 2025" → "september 2025"
            String monthYear = text.replaceAll(".*ränta\\s*(för\\s*)?", "").trim();

            return parseMonthYear(monthYear);
        } catch (Exception e) {
            System.err.println("Länsförsäkringar: kunde inte extrahera månad/år (" + e.getMessage() + ")");
            return null;
        }
    }

    /** Konverterar "september 2025" till LocalDate (första dagen i månaden) */
    private LocalDate parseMonthYear(String text) {
        try {
            String[] parts = text.split("\\s+");
            if (parts.length == 2) {
                String monthName = parts[0];
                int year = Integer.parseInt(parts[1]);
                int month = switch (monthName) {
                    case "januari", "january" -> 1;
                    case "februari", "february" -> 2;
                    case "mars", "march" -> 3;
                    case "april" -> 4;
                    case "maj", "may" -> 5;
                    case "juni", "june" -> 6;
                    case "juli", "july" -> 7;
                    case "augusti", "august" -> 8;
                    case "september" -> 9;
                    case "oktober", "october" -> 10;
                    case "november" -> 11;
                    case "december" -> 12;
                    default -> 1;
                };
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception ignored) {}
        return LocalDate.now();
    }
}