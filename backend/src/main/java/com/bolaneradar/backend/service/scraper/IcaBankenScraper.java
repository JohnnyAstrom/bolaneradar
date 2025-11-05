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
 * Webbskrapare för ICA Banken.
 * Hämtar både listräntor (överst på sidan) och endast den senaste månadens snitträntor.
 */
@Service
public class IcaBankenScraper implements BankScraper {

    private static final String URL = "https://www.icabanken.se/lana/bolan/bolanerantor/";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();

        // === Hämta listräntor (första tabellen) ===
        Element listTable = doc.selectFirst("table");
        if (listTable != null) {
            parseListRates(listTable, bank, rates);
        }

        // === Hämta snitträntor (tabellen efter "Snitträntor för bolån") ===
        Element snittHeader = doc.selectFirst("h2:matchesOwn((?i)Snitträntor för bolån)");
        if (snittHeader != null) {
            Element snittTable = snittHeader.parent().selectFirst("table");
            if (snittTable != null) {
                parseLatestAverageRates(snittTable, bank, rates);
            } else {
                System.out.println("ICA Banken: kunde inte hitta tabellen för snitträntor.");
            }
        } else {
            System.out.println("ICA Banken: kunde inte hitta rubriken 'Snitträntor för bolån'.");
        }

        long listCount = rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).count();
        long avgCount = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).count();

        System.out.println("ICA Banken: hittade totalt " + rates.size() +
                " räntor (" + listCount + " list, " + avgCount + " snitt).");

        return rates;
    }

    /** Hämtar listräntor */
    private void parseListRates(Element table, Bank bank, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 2) continue;

            String termText = cols.get(0).text().toLowerCase();
            String rateText = cols.get(1).text().replace("%", "").replace(",", ".").trim();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            if (term == null || rateText.isEmpty()) continue;

            try {
                BigDecimal rate = new BigDecimal(rateText);
                rates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
            } catch (NumberFormatException ignored) {}
        }
    }

    /** Hämtar enbart senaste månadens snitträntor */
    private void parseLatestAverageRates(Element table, Bank bank, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");
        if (rows.isEmpty()) return;

        // Första raden = senaste månaden
        Element latestRow = rows.first();
        Elements cols = latestRow.select("td");
        Elements headers = table.select("thead th, thead td");

        if (cols.isEmpty() || headers.isEmpty()) return;

        String dateText = cols.get(0).text().trim();
        LocalDate date = parseYearMonth(dateText);
        System.out.println("ICA Banken: snitträntor gäller månad " + date);

        for (int i = 1; i < cols.size() && i < headers.size(); i++) {
            String headerText = headers.get(i).text().toLowerCase();
            String rateText = cols.get(i).text().replace("%", "").replace(",", ".").trim();

            if (rateText.isEmpty() || rateText.equals("-*")) continue;

            MortgageTerm term = ScraperUtils.parseTerm(headerText);
            if (term == null) continue;

            try {
                BigDecimal rate = new BigDecimal(rateText);
                rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, date));
            } catch (NumberFormatException ignored) {}
        }
    }

    /** Konverterar t.ex. "2025 09" till LocalDate.of(2025, 9, 1) */
    private LocalDate parseYearMonth(String text) {
        try {
            String[] parts = text.split("\\s+");
            if (parts.length >= 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            System.err.println("ICA Banken: kunde inte tolka år/månad: " + text);
        }
        return LocalDate.now();
    }
}