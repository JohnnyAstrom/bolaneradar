package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
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
 *
 * Använder ScraperUtils för parsing, men behåller egen parseMonthYear()
 * eftersom Danske Bank har unikt datumformat ("Oktober 2025").
 */
@Service
public class DanskeBankScraper implements BankScraper {

    private static final String URL =
            "https://danskebank.se/privat/produkter/bolan/relaterat/aktuella-bolanerantor";

    @Override
    public String getBankName() {
        return "Danske Bank";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        // Hämta dokument via ScraperUtils
        Document doc = ScraperUtils.fetchDocument(URL);
        Elements articles = doc.select("article.responsive-nav-article");

        boolean addedAverage = false;

        for (Element article : articles) {
            String heading = "";
            Element headingEl = article.previousElementSibling();
            if (headingEl != null && "button".equals(headingEl.tagName())) {
                heading = headingEl.text().toLowerCase();
            }

            RateType rateType;
            if (heading.contains("snitt") || heading.contains("genomsnitt")) {
                rateType = RateType.AVERAGERATE;
                if (addedAverage) continue;
            } else if (heading.contains("list") || heading.contains("aktuell")) {
                rateType = RateType.LISTRATE;
            } else {
                continue;
            }

            Elements rows = article.select("tbody tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.isEmpty()) continue;

                // --- SNITTRÄNTOR ---
                if (rateType == RateType.AVERAGERATE) {
                    if (addedAverage) break;
                    if (cols.size() < 2) continue;

                    // Datum (månad + år)
                    LocalDate date = parseMonthYear(cols.get(0).text());
                    if (date == null) continue;

                    // Bindningstider
                    MortgageTerm[] terms = {
                            MortgageTerm.VARIABLE_3M,
                            MortgageTerm.FIXED_1Y,
                            MortgageTerm.FIXED_2Y,
                            MortgageTerm.FIXED_3Y,
                            MortgageTerm.FIXED_4Y,
                            MortgageTerm.FIXED_5Y,
                            MortgageTerm.FIXED_10Y
                    };

                    // Hämta endast första raden (senaste månaden)
                    for (int i = 1; i < cols.size() && i <= terms.length; i++) {
                        BigDecimal rate = ScraperUtils.parseRate(cols.get(i).text());
                        if (rate != null) {
                            rates.add(new MortgageRate(bank, terms[i - 1], RateType.AVERAGERATE, rate, date));
                        }
                    }

                    addedAverage = true;
                    continue;
                }

                // --- LISTRÄNTOR ---
                if (rateType == RateType.LISTRATE && cols.size() >= 4) {
                    MortgageTerm term = ScraperUtils.parseTerm(cols.get(0).text());
                    BigDecimal rate = ScraperUtils.parseRate(cols.get(3).text());
                    if (term != null && rate != null) {
                        rates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
                    }
                }
            }
        }

        ScraperUtils.logResult("Danske Bank", rates.size());
        return rates;
    }

    /** Konverterar t.ex. "Oktober 2025" till LocalDate (2025-10-01).
     *  Behålls lokalt eftersom Danske Bank använder unikt format. */
    private LocalDate parseMonthYear(String text) {
        try {
            String[] parts = text.trim().split("\\s+");
            if (parts.length >= 2) {
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
                    default -> 0;
                };
                return month > 0 ? LocalDate.of(year, month, 1) : null;
            }
        } catch (Exception e) {
            System.err.println("Danske Bank: kunde inte tolka månad/år: " + text);
        }
        return null;
    }
}