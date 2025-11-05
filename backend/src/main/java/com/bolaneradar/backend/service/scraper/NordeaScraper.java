package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
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

        // Standardiserad hämtning
        Document doc = ScraperUtils.fetchDocument(URL);

        // Försök hitta månadskod (YYYYMM) i snitträntetabell; fallback till svensk månad i fri text
        LocalDate avgRateMonth = extractAverageRateMonth(doc);
        if (avgRateMonth == null) {
            avgRateMonth = ScraperUtils.parseSwedishMonth(doc.text()).atDay(1);
        }

        Elements tables = doc.select("table");

        for (Element table : tables) {
            // Hoppa över irrelevanta tabeller
            String tableText = table.text().toLowerCase();
            if (tableText.contains("takränta") || tableText.contains("premie")) {
                continue;
            }

            // Bestäm typ via kontext (caption + närliggande rubrik)
            String caption = table.selectFirst("caption") != null
                    ? Objects.requireNonNull(table.selectFirst("caption")).text().toLowerCase()
                    : "";

            String heading = "";
            Element prev = table.previousElementSibling();
            int hops = 0;
            while (prev != null && hops < 5) {
                if (prev.tagName().matches("h2|h3|h4|strong|p")) {
                    heading = prev.text().toLowerCase();
                    break;
                }
                prev = prev.previousElementSibling();
                hops++;
            }

            String contextText = (caption + " " + heading).trim();
            RateType rateType = (contextText.contains("snittr") || contextText.contains("genomsnitt"))
                    ? RateType.AVERAGERATE
                    : RateType.LISTRATE;

            // Rader
            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                String termText = cols.get(0).text();
                String rateText = cols.get(1).text();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);

                if (term != null && rate != null) {
                    LocalDate dateToUse = (rateType == RateType.AVERAGERATE) ? avgRateMonth : LocalDate.now();
                    rates.add(new MortgageRate(bank, term, rateType, rate, dateToUse));
                }
            }
        }

        ScraperUtils.logResult("Nordea", rates.size());
        return rates;
    }

    /**
     * Försöker hitta en månadskod på sidan, t.ex. "202510" (oktober 2025),
     * och returnerar LocalDate motsvarande den månadens första dag.
     */
    private LocalDate extractAverageRateMonth(Document doc) {
        // Försök hitta snitträntetabell via textmatch
        Element table = doc.selectFirst("table:matchesOwn((?i)snittr|genomsnitt)");
        if (table == null) {
            // Fallback via attribut
            table = doc.selectFirst("table[title*=Snitträntor i], table[title*=Snitträntor]");
        }
        if (table == null) {
            System.out.println("Ingen snitträntetabell hittades på Nordea-sidan.");
            return null;
        }

        // Plocka den andra TH (kolumnrubrik) som innehåller något i stil med YYYYMM
        Element codeTh = table.selectFirst("thead th:nth-of-type(2)");
        if (codeTh == null) {
            System.out.println("Ingen månadskod hittades i tabellhuvudet.");
            return null;
        }

        String digits = codeTh.text().replaceAll("[^0-9]", "").trim();
        if (digits.length() < 6) {
            System.out.println("Ogiltig kod i TH: " + codeTh.text());
            return null;
        }

        String code = digits.substring(0, 6); // YYYYMM
        try {
            int year = Integer.parseInt(code.substring(0, 4));
            int month = Integer.parseInt(code.substring(4, 6));
            if (month < 1 || month > 12) return null;
            return LocalDate.of(year, month, 1);
        } catch (Exception e) {
            System.out.println("Kunde inte tolka kod: " + code);
            return null;
        }
    }
}