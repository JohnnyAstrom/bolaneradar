package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SkandiabankenScraper implements BankScraper {

    private static final String URL =
            "https://www.skandia.se/lana/bolan/bolanerantor";

    private static final Pattern PAGE_CONTENT_PATTERN =
            Pattern.compile("SKB\\.pageContent\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = ScraperUtils.fetchDocument(URL);
        String html = doc.html();

        Matcher matcher = PAGE_CONTENT_PATTERN.matcher(html);
        if (!matcher.find()) {
            return rates;
        }

        JsonNode root = mapper.readTree(matcher.group(1));

        // =====================
        // SNITTRÄNTOR
        // =====================
        JsonNode sectionContent1 = root.path("sectionContent1");
        if (sectionContent1.isArray()) {
            for (JsonNode block : sectionContent1) {
                JsonNode expanded = block.path("contentLink").path("expanded");
                String name = expanded.path("name").asText();

                if (name.toLowerCase().contains("snit")) {

                    YearMonth averageMonth =
                            ScraperUtils.parseSwedishMonth(name);

                    LocalDate averageEffectiveDate =
                            averageMonth != null
                                    ? averageMonth.atDay(1)
                                    : null;

                    parseRateTable(
                            bank,
                            expanded,
                            RateType.AVERAGERATE,
                            averageEffectiveDate,
                            rates
                    );
                }
            }
        }

        // =====================
        // LISTRÄNTOR
        // =====================
        JsonNode sectionContent2 = root.path("sectionContent2");
        if (sectionContent2.isArray()) {
            for (JsonNode block : sectionContent2) {
                JsonNode expanded = block.path("contentLink").path("expanded");
                String name = expanded.path("name").asText();

                if ("Listräntor".equalsIgnoreCase(name)) {
                    parseRateTable(
                            bank,
                            expanded,
                            RateType.LISTRATE,
                            LocalDate.now(),
                            rates
                    );
                }
            }
        }

        ScraperUtils.logResult("Skandiabanken", rates.size());
        return rates;
    }

    /**
     * Parser för Skandias TableBlock-struktur:
     * columns[] -> cells[]
     */
    private void parseRateTable(
            Bank bank,
            JsonNode block,
            RateType rateType,
            LocalDate effectiveDate,
            List<MortgageRate> out
    ) {
        JsonNode columns = block.path("columns");
        if (!columns.isArray() || columns.size() < 2) {
            return;
        }

        JsonNode termCells = columns.get(0)
                .path("contentLink")
                .path("expanded")
                .path("cells");

        JsonNode rateCells = columns.get(1)
                .path("contentLink")
                .path("expanded")
                .path("cells");

        for (int i = 0; i < termCells.size(); i++) {
            String termText = Jsoup.parse(termCells.get(i).asText()).text();
            String rateText = Jsoup.parse(rateCells.get(i).asText()).text();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term == null || rate == null) {
                continue;
            }

            out.add(
                    new MortgageRate(
                            bank,
                            term,
                            rateType,
                            rate,
                            effectiveDate
                    )
            );
        }
    }

    @Override
    public String toString() {
        return "Skandiabanken";
    }
}