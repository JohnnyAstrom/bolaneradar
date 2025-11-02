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
 * Hämtar både listräntor och snitträntor från Nordeas bolånesida.
 * Hoppar över tomma eller ogiltiga värden (t.ex. "N/A" under uppdatering).
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

        Elements tables = doc.select("table");

        for (Element table : tables) {
            // Hoppa över tabellen "Bolån med räntetak"
            String tableText = table.text().toLowerCase();
            if (tableText.contains("takränta") || tableText.contains("premie")) {
                System.out.println("Hoppar över tabell: Bolån med räntetak");
                continue;
            }

            // Avgör typ av tabell
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

                // 🛡️ hoppa över tomma, "n/a" eller ogiltiga värden
                if (rateText.isEmpty() || rateText.contains("n") || rateText.contains("-")) continue;

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);

                if (term != null && rate != null) {
                    rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                }
            }
        }

        System.out.println("Nordea: hittade " + rates.size() + " räntor.");
        return rates;
    }
}