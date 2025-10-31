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
 * Hämtar både genomsnittliga (snitträntor) och aktuella (listräntor)
 * från den regionala webbsidan.
 */
@Service
public class LansforsakringarScraper implements BankScraper {

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

        Elements tables = doc.select("table.lf-table");
        System.out.println("Länsförsäkringar: hittade " + tables.size() + " tabeller.");

        for (int i = 0; i < tables.size(); i++) {
            Element table = tables.get(i);
            RateType rateType = (i == 0) ? RateType.AVERAGERATE : RateType.LISTRATE;

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
                    rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
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
}