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
 * Webbskrapare för Alandsbanken.
 * Hämtar både listräntor och senaste månadens snitträntor.
 */
@Service
public class AlandsbankenScraper implements BankScraper {

    private static final String URL = "https://www.alandsbanken.se/banktjanster/lana-pengar/bolan";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        System.out.println("Startar skrapning för Alandsbanken...");
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(15000)
                .get();

        Elements tables = doc.select("table");
        if (tables.isEmpty()) {
            System.out.println("Ingen tabell hittad på sidan.");
            return rates;
        }

        // 1) Listräntor (första tabellen)
        Element listTable = tables.first();
        extractRatesFromTable(bank, listTable, RateType.LISTRATE, rates);
        System.out.println("Hämtade listräntor");

        // 2) Snitträntor (tabell nära “Genomsnittlig bolåneränta”)
        Element avgRateTable = null;
        Elements headers = doc.select("h2, h3, h4, p");

        for (Element header : headers) {
            String text = header.text().toLowerCase();
            if (text.contains("genomsnittlig bolåneränta") || text.contains("snittränta")) {
                avgRateTable = header.nextElementSibling();
                while (avgRateTable != null && !avgRateTable.tagName().equals("table")) {
                    avgRateTable = avgRateTable.nextElementSibling();
                }
                break;
            }
        }

        if (avgRateTable != null) {
            extractRatesFromTable(bank, avgRateTable, RateType.AVERAGERATE, rates);
            System.out.println("Hämtade snitträntor (senaste månaden)");
        } else {
            System.out.println("Hittade ingen tabell för snitträntor.");
        }

        System.out.println("🏁 Alandsbanken: totalt " + rates.size() + " räntor hittade.");
        return rates;
    }

    /** Extraherar rader ur en tabell. För AVERAGERATE tas datum från kolumn "Månad" (t.ex. "Oktober 2025"). */
    private void extractRatesFromTable(Bank bank, Element table, RateType rateType, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");

        int added = 0;
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 2) continue;

            String termText = cols.get(0).text().toLowerCase().trim();
            String rateText = cols.get(1).text()
                    .replace("\u00a0", " ")
                    .replace("%", "")
                    .replace(",", ".")
                    .trim();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);
            if (term == null || rate == null) continue;

            LocalDate date = LocalDate.now();
            // För snittränta: hämta "Månad" i kolumn 3 om den finns (t.ex. "Oktober 2025")
            if (rateType == RateType.AVERAGERATE && cols.size() >= 3) {
                String monthText = cols.get(2).text().trim().toLowerCase();
                LocalDate parsed = parseMonthYear(monthText);
                if (parsed != null) date = parsed;
            }

            rates.add(new MortgageRate(bank, term, rateType, rate, date));
            System.out.println("→ " + rateType + ": " + term + " = " + rate + "% (" + date + ")");
            added++;

            // Endast första giltiga raden för snitträntor (senaste månaden)
            if (rateType == RateType.AVERAGERATE && added > 0) break;
        }
    }

    /** "Oktober 2025" -> 2025-10-01. Hanterar svenska och engelska månadsnamn i fall sidan översätts. */
    private LocalDate parseMonthYear(String text) {
        if (text == null || text.isEmpty()) return null;
        String[] parts = text.split("\\s+");
        if (parts.length < 2) return null;

        String monthName = parts[0].toLowerCase();
        String yearStr = parts[1].replaceAll("[^0-9]", "");
        if (yearStr.isEmpty()) return null;

        int year = Integer.parseInt(yearStr);
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
            default -> 0;
        };
        if (month == 0) return null;

        return LocalDate.of(year, month, 1);
    }
}