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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Webbskrapare för Swedbank.
 *
 * Design:
 *  - LISTRATE: hämtas från bolåneräntesidan (listräntor-tabellen)
 *  - AVERAGERATE: hämtas från "Historiska genomsnittsräntor"
 *    och tar senaste månaden (första raden i tabellen).
 *
 * Varför:
 *  - Att parsa "månad" från fri text på bolåneräntesidan kan ge fel
 *    (t.ex. nämner sidan kommande uppdateringar).
 *  - Historik-sidan har stabil struktur med månad som rad (t.ex. "jan. 2026").
 */
@Service
public class SwedbankScraper implements BankScraper {

    private static final String LIST_URL =
            "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor.html";

    private static final String AVERAGE_URL =
            "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor/historiska-genomsnittsrantor.html";

    @Override
    public String getBankName() {
        return "Swedbank";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        // 1) LISTRÄNTOR (från LIST_URL)
        Document listDoc = ScraperUtils.fetchDocument(LIST_URL);
        extractListRates(listDoc, bank, rates);

        // 2) SNITTRÄNTOR (från AVERAGE_URL)
        Document avgDoc = ScraperUtils.fetchDocument(AVERAGE_URL);
        extractLatestAverageRates(avgDoc, bank, rates);

        ScraperUtils.logResult("Swedbank", rates.size());
        return rates;
    }

    private void extractListRates(Document doc, Bank bank, List<MortgageRate> out) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String caption = table.selectFirst("caption") != null
                    ? table.selectFirst("caption").text().toLowerCase()
                    : "";

            String heading = "";
            Element prev = table.previousElementSibling();
            int depth = 0;
            while (prev != null && depth < 5) {
                if (prev.tagName().matches("h2|h3|h4|strong|p")) {
                    heading = prev.text().toLowerCase();
                    break;
                }
                prev = prev.previousElementSibling();
                depth++;
            }

            String context = (caption + " " + heading).trim();

            // Vi vill bara ha listräntetabellen här
            if (!(context.contains("aktuella") || context.contains("list"))) {
                continue;
            }

            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                MortgageTerm term = ScraperUtils.parseTerm(cols.get(0).text());
                BigDecimal rate = ScraperUtils.parseRate(cols.get(1).text());

                if (term == null || rate == null) continue;

                out.add(new MortgageRate(
                        bank,
                        term,
                        RateType.LISTRATE,
                        rate,
                        LocalDate.now()
                ));
            }
        }
    }

    /**
     * Hämtar senaste månaden från historik-tabellen (första raden).
     * Tabellen brukar vara:
     *  Rad 1: "jan. 2026" + värden för 3 mån, 1 år, 2 år, ...
     */
    private void extractLatestAverageRates(Document doc, Bank bank, List<MortgageRate> out) {
        Element table = doc.selectFirst("table");
        if (table == null) {
            System.out.println("Swedbank: ingen tabell hittades på historik-sidan för snitträntor.");
            return;
        }

        Elements headerCells = table.select("thead th");
        if (headerCells.isEmpty()) {
            System.out.println("Swedbank: saknar table header (thead th) på historik-sidan.");
            return;
        }

        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");
        if (rows.isEmpty()) {
            System.out.println("Swedbank: inga rader hittades i historik-tabellen.");
            return;
        }

        // Första raden = senaste månaden
        Element latestRow = rows.first();
        Elements cols = latestRow.select("td");
        if (cols.size() < 2) {
            System.out.println("Swedbank: senaste raden i historik-tabellen har för få kolumner.");
            return;
        }

        // Kolumn 0 = månadstext (t.ex. "jan. 2026")
        String monthText = cols.get(0).text().toLowerCase();
        YearMonth ym = ScraperUtils.parseSwedishMonth(monthText);

        if (ym == null) {
            System.out.println("Swedbank: kunde inte tolka månad från historik-rad: " + monthText);
            return;
        }

        LocalDate effectiveDate = ym.minusMonths(1).atDay(1);

        // Skydd: framtida snitträntor får inte förekomma
        if (effectiveDate.isAfter(LocalDate.now())) {
            System.out.println("Swedbank: ignorerar framtida snittränta " + effectiveDate);
            return;
        }

        // Header: första kolumnen är "Bindningstid" eller tom, sen termer
        // Cols: första kolumnen är månad, sen värden i samma ordning som headers
        for (int i = 1; i < cols.size() && i < headerCells.size(); i++) {
            String termText = headerCells.get(i).text();
            String rateText = cols.get(i).text();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term == null || rate == null) continue;

            out.add(new MortgageRate(
                    bank,
                    term,
                    RateType.AVERAGERATE,
                    rate,
                    effectiveDate
            ));
        }
    }

    @Override
    public String toString() {
        return "Swedbank";
    }
}