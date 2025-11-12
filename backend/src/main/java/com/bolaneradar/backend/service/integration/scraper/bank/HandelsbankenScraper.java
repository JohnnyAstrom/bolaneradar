package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.*;
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
 * Webbskrapare för Handelsbanken.
 * Hämtar både listränta och snittränta från tabellen,
 * samt tolkar aktuell månad (snittränta) från sidan.
 */
@Service
public class HandelsbankenScraper implements BankScraper {

    private static final String URL = "https://www.handelsbanken.se/sv/privat/bolan/bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = ScraperUtils.fetchDocument(URL);

        // Snitträntans månad – försök hitta via fält/text, fallback till helsidestext
        LocalDate avgRateMonth = extractSelectedMonth(doc);
        if (avgRateMonth == null) {
            YearMonth ym = ScraperUtils.parseSwedishMonth(doc.text());
            avgRateMonth = ym.atDay(1);
        }

        Element table = doc.selectFirst("table");
        if (table == null) return rates;

        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) rows = table.select("tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) continue;

            String termText = cols.get(0).text();
            String listRateText = cols.get(1).text();
            String avgRateText  = cols.get(2).text();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal listRate = ScraperUtils.parseRate(listRateText);
            BigDecimal avgRate  = ScraperUtils.parseRate(avgRateText);

            if (term != null && listRate != null) {
                rates.add(new MortgageRate(bank, term, RateType.LISTRATE, listRate, LocalDate.now()));
            }
            if (term != null && avgRate != null) {
                rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, avgRate, avgRateMonth));
            }
        }

        ScraperUtils.logResult("Handelsbanken", rates.size());
        return rates;
    }

    /**
     * Försöker läsa ut vald månad från sidan (ex. "September 2025") och returnera första dagen i månaden.
     */
    private LocalDate extractSelectedMonth(Document doc) {
        // Ex. ett input- eller select-fält som visar vald period
        Element input = doc.selectFirst("input#period-selector, input[name=period], select#period-selector option[selected]");
        if (input != null) {
            String value = input.hasAttr("value") ? input.attr("value") : input.text();
            if (value != null && !value.isBlank()) {
                YearMonth ym = ScraperUtils.parseSwedishMonth(value);
                return ym.atDay(1);
            }
        }
        // Som backup, prova caption eller intilliggande texter
        Element caption = doc.selectFirst("caption");
        if (caption != null) {
            YearMonth ym = ScraperUtils.parseSwedishMonth(caption.text());
            return ym.atDay(1);
        }
        return null;
    }
}