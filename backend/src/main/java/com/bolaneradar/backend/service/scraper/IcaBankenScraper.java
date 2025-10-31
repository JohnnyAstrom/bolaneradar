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
 * Webbskrapare för ICA Banken.
 * Hämtar både listräntor och snitträntor från samma sida.
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

        // === Hämta snitträntor (tabellen efter h2 med text "Snitträntor för bolån") ===
        Element snittSection = doc.selectFirst("h2:matchesOwn((?i)Snitträntor för bolån)");
        if (snittSection != null) {
            assert snittSection.parent() != null;
            Element snittTable = snittSection.parent().selectFirst("table");
            if (snittTable != null) {
                parseAverageRates(snittTable, bank, rates);
            } else {
                System.out.println("Kunde inte hitta tabellen under 'Snitträntor för bolån'");
            }
        } else {
            System.out.println("Kunde inte hitta rubriken 'Snitträntor för bolån'");
        }

        long listCount = rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).count();
        long avgCount = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).count();

        System.out.println("ICA Banken: hittade totalt " + rates.size() +
                " räntor (" + listCount + " list, " + avgCount + " snitt).");

        return rates;
    }

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

    private void parseAverageRates(Element table, Bank bank, List<MortgageRate> rates) {
        Elements headerCols = table.select("thead td");
        Elements firstRow = Objects.requireNonNull(table.select("tbody tr").first()).select("td");

        if (headerCols.size() <= 1) return;

        for (int i = 1; i < headerCols.size() && i < firstRow.size(); i++) {
            String termText = headerCols.get(i).text().toLowerCase();
            String rateText = firstRow.get(i).text().replace("%", "").replace(",", ".").trim();

            if (rateText.equals("-*") || rateText.isEmpty()) continue;

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            if (term == null) continue;

            try {
                BigDecimal rate = new BigDecimal(rateText);
                rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, LocalDate.now()));
            } catch (NumberFormatException ignored) {}
        }
    }
}