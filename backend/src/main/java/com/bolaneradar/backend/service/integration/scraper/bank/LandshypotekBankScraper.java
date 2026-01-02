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

@Service
public class LandshypotekBankScraper implements BankScraper {

    private static final String URL =
            "https://www.landshypotek.se/lana/bolanerantor/";

    @Override
    public String getBankName() {
        return "Landshypotek Bank";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = ScraperUtils.fetchDocument(URL);

        // =========================
        // LISTRÄNTOR
        // =========================
        Element listRateBlock = doc.getElementById("anchor-2607");
        if (listRateBlock != null) {
            Element table = listRateBlock.selectFirst("table");
            if (table != null) {
                extractSimpleTable(
                        bank,
                        table,
                        RateType.LISTRATE,
                        LocalDate.now(),
                        rates
                );
            }
        }

        // =========================
        // SNITTRÄNTOR (föregående månad)
        // =========================
        Element avgRateBlock = doc.getElementById("anchor-2595");
        if (avgRateBlock != null) {

            Element monthHeading = avgRateBlock.selectFirst("h4");
            if (monthHeading != null) {
                String monthText = monthHeading.text().toLowerCase();

                /*
                 * Landshypotek publicerar alltid föregående månads snittränta.
                 * Vi tar därför:
                 *  - nuvarande datum
                 *  - minus 1 månad
                 *  - använder år + månad från rubriken
                 */
                YearMonth previousMonth = YearMonth.now().minusMonths(1);

                YearMonth effectiveMonth =
                        ScraperUtils.parseSwedishMonth(
                                monthText + " " + previousMonth.getYear()
                        );

                LocalDate effectiveDate = effectiveMonth.atDay(1);

                Element table = avgRateBlock.selectFirst("table");
                if (table != null) {
                    extractSimpleTable(
                            bank,
                            table,
                            RateType.AVERAGERATE,
                            effectiveDate,
                            rates
                    );
                }
            }
        }

        ScraperUtils.logResult("Landshypotek Bank", rates.size());
        return rates;
    }

    /**
     * Enkel tabellparser:
     *  - kolumn 0 = bindningstid
     *  - kolumn 1 = ränta
     */
    private void extractSimpleTable(
            Bank bank,
            Element table,
            RateType rateType,
            LocalDate effectiveDate,
            List<MortgageRate> out
    ) {
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) {
            rows = table.select("tr");
        }

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 2) continue;

            String termText = cols.get(0).text();
            String rateText = cols.get(1).text();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term == null || rate == null) continue;

            out.add(new MortgageRate(
                    bank,
                    term,
                    rateType,
                    rate,
                    effectiveDate
            ));
        }
    }

    @Override
    public String toString() {
        return "Landshypotek Bank";
    }
}