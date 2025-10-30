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
 * Hämtar deras aktuella bolåneräntor från nordeas webbplats.
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
        // Hitta alla tabeller
        Elements tables = doc.select("table");

        for (Element table : tables) {
            // Hoppa över tabellen "Bolån med räntetak"
            String tableText = table.text().toLowerCase();
            if (tableText.contains("takränta") || tableText.contains("premie")) {
                System.out.println("Hoppar över tabell: Bolån med räntetak");
                continue;
            }

            // Försök avgöra om det är snitträntor eller listräntor
            String contextText = table.previousElementSibling() != null
                    ? Objects.requireNonNull(table.previousElementSibling()).text().toLowerCase()
                    : "";

            RateType rateType = contextText.contains("snittränt") ? RateType.AVERAGERATE : RateType.LISTRATE;

            Elements rows = table.select("tbody tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() >= 2) {
                    String termText = cols.get(0).text().toLowerCase();
                    String rateText = cols.get(1).text()
                            .replace("%", "")
                            .replace(",", ".")
                            .trim();

                    MortgageTerm term = ScraperUtils.parseTerm(termText);
                    if (term != null && !rateText.isEmpty()) {
                        BigDecimal rate = new BigDecimal(rateText);
                        rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                    }
                }
            }
        }

        System.out.println("Nordea: hittade " + rates.size() + " räntor.");
        return rates;
    }
}