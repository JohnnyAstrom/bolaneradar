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
 * Webbskrapare för Handelsbanken.
 * Hämtar listränta och snittränta från samma tabell.
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

        // Hitta den centrala tabellen
        Element table = doc.selectFirst("table");
        if (table == null) {
            System.out.println("Ingen tabell hittades på Handelsbankens sida.");
            return rates;
        }

        Elements rows = table.select("tbody tr");
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() >= 3) {
                String termText = cols.get(0).text().toLowerCase();

                // Läs både list- och snittränta
                String listRateText = cols.get(1).text().replace("%", "").replace(",", ".").trim();
                String avgRateText = cols.get(2).text().replace("%", "").replace(",", ".").trim();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                if (term == null) continue;

                // Listränta
                if (!listRateText.isEmpty()) {
                    try {
                        BigDecimal listRate = new BigDecimal(listRateText);
                        rates.add(new MortgageRate(bank, term, RateType.LISTRATE, listRate, LocalDate.now()));
                    } catch (NumberFormatException ignored) {}
                }

                // Snittränta
                if (!avgRateText.isEmpty()) {
                    try {
                        BigDecimal avgRate = new BigDecimal(avgRateText);
                        rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, avgRate, LocalDate.now()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        System.out.println("Handelsbanken: hittade " + rates.size() + " räntor totalt.");
        return rates;
    }
}