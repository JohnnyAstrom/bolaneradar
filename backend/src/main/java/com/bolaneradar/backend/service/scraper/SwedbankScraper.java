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

        // Hitta alla tabeller
        Elements tables = doc.select("table");
        System.out.println("🔍 Antal tabeller hittade: " + tables.size());

        for (Element table : tables) {
            // Kolla om tabellen har en caption eller rubrik i närheten
            String caption = "";
            Element capEl = table.selectFirst("caption");
            if (capEl != null) caption = capEl.text().toLowerCase();

            // Om inget caption, försök hitta rubrik före tabellen
            String heading = "";
            Element prev = table.previousElementSibling();
            int checkDepth = 0;
            while (prev != null && checkDepth < 5) { // titta några steg bakåt
                if (prev.tagName().matches("h2|h3|h4|strong|p")) {
                    heading = prev.text().toLowerCase();
                    break;
                }
                prev = prev.previousElementSibling();
                checkDepth++;
            }

            String context = caption + " " + heading;

            // Identifiera typ baserat på text
            RateType rateType;
            if (context.contains("genomsnitt")) {
                rateType = RateType.AVERAGERATE;
            } else if (context.contains("aktuella") || context.contains("list")) {
                rateType = RateType.LISTRATE;
            } else {
                continue; // hoppa över tabeller utan relevant rubrik
            }

            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr"); // fallback

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
                        rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        System.out.println("Swedbank: hittade " + rates.size() + " räntor totalt.");
        return rates;
    }
}
