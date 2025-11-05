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
import java.util.Objects;

/**
 * Webbskrapare för Nordea.
 * Hämtar både listräntor och snitträntor.
 * Identifierar även vilken månad snitträntorna gäller (ex. 202510 → oktober 2025).
 */
@Service
public class NordeaScraper implements BankScraper {

    private static final String URL = "https://www.nordea.se/privat/produkter/bolan/bolanerantor.html";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();

        // 🔹 Försök hitta en månadskod, ex "202510"
        LocalDate avgRateMonth = extractAverageRateMonth(doc);
        if (avgRateMonth == null) {
            System.out.println("Kunde inte hitta månad för Nordeas snittränta, använder dagens datum.");
            avgRateMonth = LocalDate.now();
        }

        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (tableText.contains("takränta") || tableText.contains("premie")) {
                continue;
            }

            String contextText = table.previousElementSibling() != null
                    ? Objects.requireNonNull(table.previousElementSibling()).text().toLowerCase()
                    : "";

            RateType rateType = contextText.contains("snittränt") ? RateType.AVERAGERATE : RateType.LISTRATE;

            Elements rows = table.select("tbody tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                String termText = cols.get(0).text().toLowerCase().trim();
                String rateText = cols.get(1).text()
                        .replace("%", "")
                        .replace(",", ".")
                        .trim()
                        .toLowerCase();

                if (rateText.isEmpty() || rateText.contains("n") || rateText.contains("-")) continue;

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);

                if (term != null && rate != null) {
                    LocalDate dateToUse = (rateType == RateType.AVERAGERATE)
                            ? avgRateMonth
                            : LocalDate.now();

                    rates.add(new MortgageRate(bank, term, rateType, rate, dateToUse));
                }
            }
        }

        System.out.println("Nordea: hittade " + rates.size() + " räntor.");
        return rates;
    }

    /**
     * Försöker hitta en månadskod på sidan, t.ex. "202510" (oktober 2025),
     * och returnerar LocalDate motsvarande den månadens första dag.
     */
    private LocalDate extractAverageRateMonth(Document doc) {
        // Hitta tabellen som innehåller ordet "genomsnitt" eller "snittränt"
        Element table = doc.selectFirst("table:matchesOwn((?i)snittr|genomsnitt)");
        if (table == null) {
            // fallback – prova via title eller caption
            table = doc.selectFirst("table[title*=Snitträntor i], table[title*=Snitträntor]");
        }
        if (table == null) {
            System.out.println("⚠️ Ingen snitträntetabell hittades på Nordea-sidan.");
            return null;
        }

        // Plocka den andra TH (kolumnrubrik) som innehåller något i stil med 202510
        Element codeTh = table.selectFirst("thead th:nth-of-type(2)");
        if (codeTh == null) {
            System.out.println("⚠️ Ingen månadskod hittades i tabellhuvudet.");
            return null;
        }

        // Extrahera endast siffror (ta bort citationstecken och mellanslag)
        String digits = codeTh.text().replaceAll("[^0-9]", "").trim();

        if (digits.length() < 6) {
            System.out.println("⚠️ Ogiltig kod i TH: " + codeTh.text());
            return null;
        }

        String code = digits.substring(0, 6); // YYYYMM
        try {
            int year = Integer.parseInt(code.substring(0, 4));
            int month = Integer.parseInt(code.substring(4, 6));
            if (month < 1 || month > 12) return null;

            LocalDate parsed = LocalDate.of(year, month, 1);
            System.out.println("✅ Identifierad snitträntemånad för Nordea: " + parsed);
            return parsed;
        } catch (Exception e) {
            System.out.println("⚠️ Kunde inte tolka kod: " + code);
            return null;
        }
    }
}