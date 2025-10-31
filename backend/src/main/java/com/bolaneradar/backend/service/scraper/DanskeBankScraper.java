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
 * Webbskrapare för Danske Bank.
 * Hämtar både aktuella (listräntor) och senaste månadens snitträntor.
 */
@Service
public class DanskeBankScraper implements BankScraper {

    private static final String URL =
            "https://danskebank.se/privat/produkter/bolan/relaterat/aktuella-bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(15000)
                .get();

        Elements articles = doc.select("article.responsive-nav-article");

        boolean addedAverage = false;

        for (Element article : articles) {
            String heading = "";
            Element headingEl = article.previousElementSibling();
            if (headingEl != null && headingEl.tagName().equals("button")) {
                heading = headingEl.text().toLowerCase();
            }

            RateType rateType;
            if (heading.contains("snitt")) {
                rateType = RateType.AVERAGERATE;
                if (addedAverage) continue; // ta bara senaste månadens snitträntor
            } else if (heading.contains("list") || heading.contains("aktuell")) {
                rateType = RateType.LISTRATE;
            } else continue;

            Elements rows = article.select("tbody tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.isEmpty()) continue;

                // 🟢 Snitträntor – endast första giltiga raden
                if (rateType == RateType.AVERAGERATE) {
                    if (addedAverage) break;
                    if (cols.size() < 2) continue;
                    String monthText = cols.get(0).text().trim();
                    if (monthText.toLowerCase().contains("genomsnitt")) continue;

                    MortgageTerm[] terms = {
                            MortgageTerm.VARIABLE_3M,
                            MortgageTerm.FIXED_1Y,
                            MortgageTerm.FIXED_2Y,
                            MortgageTerm.FIXED_3Y,
                            MortgageTerm.FIXED_4Y,
                            MortgageTerm.FIXED_5Y,
                            MortgageTerm.FIXED_10Y
                    };

                    for (int i = 1; i < cols.size() && i <= terms.length; i++) {
                        String rateText = cols.get(i).wholeText()
                                .replace("%", "")
                                .replace(",", ".")
                                .replace("\u00a0", "")
                                .trim();
                        if (rateText.isEmpty() || rateText.equals("-")) continue;

                        try {
                            rates.add(new MortgageRate(bank, terms[i - 1], rateType,
                                    new BigDecimal(rateText), LocalDate.now()));
                        } catch (NumberFormatException ignored) {}
                    }
                    addedAverage = true;
                    continue;
                }

                // 🟣 Listräntor
                if (rateType == RateType.LISTRATE && cols.size() >= 4) {
                    String termText = cols.get(0).wholeText().toLowerCase().trim();
                    String rateText = cols.get(3).wholeText()
                            .replace("%", "")
                            .replace(",", ".")
                            .replace("\u00a0", "")
                            .trim();

                    MortgageTerm term = ScraperUtils.parseTerm(termText);
                    if (term != null && !rateText.isEmpty() && !rateText.equals("-")) {
                        try {
                            rates.add(new MortgageRate(bank, term, rateType,
                                    new BigDecimal(rateText), LocalDate.now()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        System.out.println("Danske Bank: hittade totalt " + rates.size() +
                " räntor (" +
                rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).count() + " list, " +
                rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).count() + " snitt).");

        return rates;
    }
}