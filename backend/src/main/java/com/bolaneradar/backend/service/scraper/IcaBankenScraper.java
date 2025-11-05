package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
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
 * Hämtar både listräntor (överst på sidan)
 * och endast den senaste månadens snitträntor.
 *
 * Använder gemensamma metoder från ScraperUtils,
 * men behåller egen parseYearMonth() då ICA har unikt datumformat.
 */
@Service
public class IcaBankenScraper implements BankScraper {

    private static final String URL = "https://www.icabanken.se/lana/bolan/bolanerantor/";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        // Hämta dokument via gemensam metod
        Document doc = ScraperUtils.fetchDocument(URL);

        // === Listräntor ===
        Element listTable = doc.selectFirst("table");
        if (listTable != null) {
            extractListRates(listTable, bank, rates);
        }

        // === Snitträntor (efter rubriken "Snitträntor för bolån") ===
        Element snittHeader = doc.selectFirst("h2:matchesOwn((?i)Snitträntor för bolån)");
        if (snittHeader != null) {
            Element snittTable = snittHeader.parent().selectFirst("table");
            if (snittTable != null) {
                extractAverageRates(snittTable, bank, rates);
            } else {
                System.out.println("ICA Banken: kunde inte hitta tabellen för snitträntor.");
            }
        } else {
            System.out.println("ICA Banken: kunde inte hitta rubriken 'Snitträntor för bolån'.");
        }

        ScraperUtils.logResult("ICA Banken", rates.size());
        return rates;
    }

    /** Hämtar listräntor (från första tabellen) */
    private void extractListRates(Element table, Bank bank, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 2) continue;

            MortgageTerm term = ScraperUtils.parseTerm(cols.get(0).text());
            BigDecimal rate = ScraperUtils.parseRate(cols.get(1).text());

            if (term != null && rate != null) {
                rates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
            }
        }
    }

    /** Hämtar endast den senaste månadens snitträntor */
    private void extractAverageRates(Element table, Bank bank, List<MortgageRate> rates) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");
        if (rows.isEmpty()) return;

        // Första raden = senaste månaden
        Element latestRow = rows.first();
        Elements cols = latestRow.select("td");
        Elements headers = table.select("thead th, thead td");
        if (cols.isEmpty() || headers.isEmpty()) return;

        LocalDate date = parseYearMonth(cols.get(0).text().trim());
        System.out.println("ICA Banken: snitträntor gäller månad " + date);

        for (int i = 1; i < cols.size() && i < headers.size(); i++) {
            MortgageTerm term = ScraperUtils.parseTerm(headers.get(i).text());
            BigDecimal rate = ScraperUtils.parseRate(cols.get(i).text());
            if (term != null && rate != null) {
                rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, date));
            }
        }
    }

    /** Konverterar t.ex. "2025 09" till LocalDate(2025-09-01).
     *  Behålls lokalt eftersom ICA använder unikt år-månad-format. */
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