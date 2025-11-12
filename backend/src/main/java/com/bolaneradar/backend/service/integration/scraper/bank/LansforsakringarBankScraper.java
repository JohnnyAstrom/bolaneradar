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
 * Webbskrapare för Länsförsäkringar.
 * Hämtar både snitträntor och listräntor.
 */
@Service
public class LansforsakringarBankScraper implements BankScraper {

    private static final String URL =
            "https://www.lansforsakringar.se/norrbotten/privat/bank/bolan/bolaneranta/";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();
        Document doc = ScraperUtils.fetchDocument(URL);

        LocalDate avgDate = extractAverageDate(doc);
        Elements tables = doc.select("table.lf-table");

        for (int i = 0; i < tables.size(); i++) {
            RateType type = (i == 0) ? RateType.AVERAGERATE : RateType.LISTRATE;
            LocalDate date = (type == RateType.AVERAGERATE && avgDate != null) ? avgDate : LocalDate.now();

            for (Element row : tables.get(i).select("tr")) {
                Elements cols = row.select("td");
                if (cols.size() < 2) continue;
                MortgageTerm term = ScraperUtils.parseTerm(cols.get(0).text());
                BigDecimal rate = ScraperUtils.parseRate(cols.get(1).text());
                if (term != null && rate != null) {
                    rates.add(new MortgageRate(bank, term, type, rate, date));
                }
            }
        }

        ScraperUtils.logResult("Länsförsäkringar", rates.size());
        return rates;
    }

    private LocalDate extractAverageDate(Document doc) {
        Element heading = doc.selectFirst(":matchesOwn((?i)genomsnittlig[a]?[a-z\\s]*ränta)");
        if (heading == null) return null;
        String text = heading.text();
        YearMonth ym = ScraperUtils.parseSwedishMonth(text);
        return ym.atDay(1);
    }
}