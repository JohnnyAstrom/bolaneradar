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
            if (heading.contains("snitt") || heading.contains("genomsnitt")) {
                rateType = RateType.AVERAGERATE;
                if (addedAverage) continue; // endast senaste snitträntan
            } else if (heading.contains("list") || heading.contains("aktuell")) {
                rateType = RateType.LISTRATE;
            } else continue;

            Elements rows = article.select("tbody tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.isEmpty()) continue;

                // 🟢 SNITTRÄNTOR
                if (rateType == RateType.AVERAGERATE) {
                    // Första giltiga raden = senaste månad
                    if (addedAverage) break;
                    if (cols.size() < 2) continue;

                    // Kolumn 0 = månad + år
                    String monthText = cols.get(0).text().trim();
                    LocalDate date = parseMonthYear(monthText);
                    if (date == null) continue;

                    // Kolumnerna efter = räntor för olika bindningstider
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
                            BigDecimal rate = new BigDecimal(rateText);
                            rates.add(new MortgageRate(bank, terms[i - 1], rateType, rate, date));
                        } catch (NumberFormatException ignored) {}
                    }
                    addedAverage = true;
                    continue;
                }

                // 🟣 LISTRÄNTOR
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

    /** Konverterar t.ex. "Oktober 2025" till LocalDate (2025-10-01) */
    private LocalDate parseMonthYear(String text) {
        try {
            String[] parts = text.split("\\s+");
            if (parts.length == 2) {
                String monthName = parts[0].toLowerCase();
                int year = Integer.parseInt(parts[1]);
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
                    default -> 1;
                };
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            System.err.println("Danske Bank: kunde inte tolka månad/år: " + text);
        }
        return null;
    }
}