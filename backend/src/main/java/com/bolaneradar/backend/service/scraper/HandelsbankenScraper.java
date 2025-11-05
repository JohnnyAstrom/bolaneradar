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
import java.util.Map;

/**
 * Webbskrapare för Handelsbanken.
 * Hämtar både listränta och snittränta från tabellen,
 * samt tolkar aktuell månad från dropdown-menyn.
 */
@Service
public class HandelsbankenScraper implements BankScraper {

    private static final String URL = "https://www.handelsbanken.se/sv/privat/bolan/bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();

        // Hämta vald månad från dropdown (snitträntan avser denna)
        LocalDate avgRateMonth = extractSelectedMonth(doc);
        if (avgRateMonth == null) {
            System.out.println("Kunde inte hitta månad för snittränta på Handelsbanken-sidan. Använder dagens datum.");
            avgRateMonth = LocalDate.now();
        }

        // Hitta tabellen med räntor
        Element table = doc.selectFirst("table");
        if (table == null) {
            System.out.println("Ingen tabell hittades på Handelsbankens sida.");
            return rates;
        }

        // Loopa igenom raderna
        Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() >= 3) {
                String termText = cols.get(0).text().toLowerCase();

                String listRateText = cols.get(1).text().replace("%", "").replace(",", ".").trim();
                String avgRateText = cols.get(2).text().replace("%", "").replace(",", ".").trim();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                if (term == null) continue;

                // Listränta (aktuella)
                if (!listRateText.isEmpty()) {
                    try {
                        BigDecimal listRate = new BigDecimal(listRateText);
                        rates.add(new MortgageRate(bank, term, RateType.LISTRATE, listRate, LocalDate.now()));
                    } catch (NumberFormatException ignored) {}
                }

                // Snittränta (för vald månad)
                if (!avgRateText.isEmpty()) {
                    try {
                        BigDecimal avgRate = new BigDecimal(avgRateText);
                        rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, avgRate, avgRateMonth));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        System.out.println("Handelsbanken: hittade " + rates.size() + " räntor totalt.");
        return rates;
    }

    /**
     * Försöker läsa ut vald månad (t.ex. "September 2025")
     * från dropdown-menyn. Returnerar LocalDate för månadens första dag.
     */
    private LocalDate extractSelectedMonth(Document doc) {
        // Försök hitta input-fältet med det vi ser på sidan
        Element input = doc.selectFirst("input#period-selector");
        if (input != null) {
            String value = input.attr("value").trim(); // ex: "September 2025"
            if (!value.isEmpty()) {
                LocalDate parsed = parseSwedishMonth(value);
                if (parsed != null) return parsed;
            }
        }

        // Som backup: sök efter något liknande "Avser månad September 2025" i hela texten
        String text = doc.text().toLowerCase();
        if (text.contains("månad")) {
            int index = text.indexOf("månad");
            String substring = text.substring(Math.max(0, index - 20), Math.min(text.length(), index + 20));
            LocalDate parsed = parseSwedishMonth(substring);
            if (parsed != null) return parsed;
        }

        return null; // Kunde inte hitta månad
    }

    /**
     * Tolkar svensk månadssträng som "September 2025" → LocalDate(2025-09-01)
     */
    private LocalDate parseSwedishMonth(String text) {
        text = text.toLowerCase();
        Map<String, Integer> months = Map.ofEntries(
                Map.entry("januari", 1),
                Map.entry("februari", 2),
                Map.entry("mars", 3),
                Map.entry("april", 4),
                Map.entry("maj", 5),
                Map.entry("juni", 6),
                Map.entry("juli", 7),
                Map.entry("augusti", 8),
                Map.entry("september", 9),
                Map.entry("oktober", 10),
                Map.entry("november", 11),
                Map.entry("december", 12)
        );

        for (var entry : months.entrySet()) {
            if (text.contains(entry.getKey())) {
                String yearText = text.replaceAll("\\D*(\\d{4}).*", "$1");
                try {
                    int year = Integer.parseInt(yearText);
                    return LocalDate.of(year, entry.getValue(), 1);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}