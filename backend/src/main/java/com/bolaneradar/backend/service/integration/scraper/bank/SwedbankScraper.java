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
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor) bolåneräntor.
 */
@Service
public class SwedbankScraper implements BankScraper {

    private static final String URL = "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor.html";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        // Hämta HTML-dokumentet via gemensam metod
        Document doc = ScraperUtils.fetchDocument(URL);

        // Hämta månad för snittränta (ex. "Genomsnittsränta, september 2025")
        YearMonth averageMonth = ScraperUtils.parseSwedishMonth(doc.text());
        LocalDate averageEffectiveDate = averageMonth.atDay(1);

        // Hitta tabeller på sidan
        Elements tables = doc.select("table");

        for (Element table : tables) {
            // Försök identifiera vilken typ av tabell (list- eller snittränta)
            String caption = table.selectFirst("caption") != null
                    ? table.selectFirst("caption").text().toLowerCase()
                    : "";

            String heading = "";
            Element prev = table.previousElementSibling();
            int checkDepth = 0;
            while (prev != null && checkDepth < 5) {
                if (prev.tagName().matches("h2|h3|h4|strong|p")) {
                    heading = prev.text().toLowerCase();
                    break;
                }
                prev = prev.previousElementSibling();
                checkDepth++;
            }

            String context = caption + " " + heading;
            RateType rateType;
            if (context.contains("genomsnitt")) {
                rateType = RateType.AVERAGERATE;
            } else if (context.contains("aktuella") || context.contains("list")) {
                rateType = RateType.LISTRATE;
            } else {
                continue; // hoppa över okända tabeller
            }

            // Loopa igenom rader i tabellen
            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;

                String termText = cols.get(0).text();
                String rateText = cols.get(1).text();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);

                if (term != null && rate != null) {
                    LocalDate effectiveDate = (rateType == RateType.AVERAGERATE)
                            ? averageEffectiveDate
                            : LocalDate.now();

                    rates.add(new MortgageRate(bank, term, rateType, rate, effectiveDate));
                }
            }
        }

        ScraperUtils.logResult("Swedbank", rates.size());
        return rates;
    }
}