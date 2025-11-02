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
 * Hämtar både aktuella (listräntor) och senaste månadens genomsnittliga (snitträntor) bolåneräntor.
 * <p>
 * Identifierar tabeller via rubriker (“Genomsnittlig bolåneränta”) och hämtar endast den senaste raden
 * för snitträntor.
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

        // === 1️⃣ Listräntor (första tabellen) ===
        Element listTable = tables.first();
        extractRatesFromTable(bank, listTable, RateType.LISTRATE, rates);
        System.out.println("Hämtade listräntor");

        // === 2️⃣ Snitträntor (tabell nära “Genomsnittlig bolåneränta”) ===
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
            System.out.println("Hämtade snitträntor (endast senaste månaden)");
        } else {
            System.out.println("Hittade ingen tabell för snitträntor.");
        }

        System.out.println("🏁 Alandsbanken: totalt " + rates.size() + " räntor hittade.");
        return rates;
    }

    /** Hjälpmetod för att extrahera rader ur en tabell */
    private void extractRatesFromTable(Bank bank, Element table, RateType rateType, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");

        int added = 0;
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 2) continue;

            String termText = cols.get(0).text().toLowerCase().trim();
            String rateText = cols.get(1).text()
                    .replace("%", "")
                    .replace(",", ".")
                    .trim();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term != null && rate != null) {
                rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                System.out.println("→ " + rateType + ": " + term + " = " + rate + "%");
                added++;
            }

            // Endast första giltiga raden för snitträntor (senaste månaden)
            if (rateType == RateType.AVERAGERATE && added > 0) break;
        }
    }
}