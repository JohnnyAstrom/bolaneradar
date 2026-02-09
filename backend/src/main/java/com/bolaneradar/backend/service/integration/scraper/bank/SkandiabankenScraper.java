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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SkandiabankenScraper implements BankScraper {

    private static final String URL =
            "https://www.skandia.se/lana/bolan/bolanerantor";

    // Mer tolerant regex – fångar hela objektet även om ; saknas
    private static final Pattern PAGE_CONTENT_PATTERN =
            Pattern.compile("SKB\\.pageContent\\s*=\\s*(\\{.*\\})", Pattern.DOTALL);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getBankName() {
        return "Skandiabanken";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        Document doc = ScraperUtils.fetchDocument(URL);
        String html = doc.html();

        Matcher matcher = PAGE_CONTENT_PATTERN.matcher(html);
        if (!matcher.find()) {
            System.out.println("Skandiabanken: kunde inte hitta SKB.pageContent i HTML");
            return rates;
        }

        JsonNode root;
        try {
            root = mapper.readTree(matcher.group(1));
        } catch (Exception e) {
            System.out.println("Skandiabanken: JSON-parse misslyckades");
            return rates;
        }

        // Iterera över ALLA sektioner dynamiskt
        Iterator<Map.Entry<String, JsonNode>> sections = root.fields();
        while (sections.hasNext()) {
            Map.Entry<String, JsonNode> entry = sections.next();
            JsonNode section = entry.getValue();

            if (!section.isArray()) continue;

            for (JsonNode block : section) {
                JsonNode expanded = block.path("contentLink").path("expanded");
                if (expanded.isMissingNode()) continue;

                String name = expanded.path("name").asText("").toLowerCase();

                // ===== SNITTRÄNTOR =====
                if (name.contains("snit") || name.contains("genomsnitt")) {
                    YearMonth month = ScraperUtils.parseSwedishMonth(name);
                    if (month == null) {
                        System.out.println("Skandiabanken: kunde inte tolka månad från '" + name + "'");
                        continue;
                    }

                    parseRateTable(
                            bank,
                            expanded,
                            RateType.AVERAGERATE,
                            month.atDay(1),
                            rates
                    );
                }

                // ===== LISTRÄNTOR =====
                if (name.contains("listränt")) {
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
     * Parser för Skandias table-block:
     * columns[] -> contentLink.expanded.cells[]
     */
    private void parseRateTable(
            Bank bank,
            JsonNode block,
            RateType rateType,
            LocalDate effectiveDate,
            List<MortgageRate> out
    ) {
        if (effectiveDate == null) {
            System.out.println("Skandiabanken: effectiveDate saknas – hoppar över block");
            return;
        }

        JsonNode columns = block.path("columns");
        if (!columns.isArray() || columns.size() < 2) return;

        JsonNode termCells = columns.get(0)
                .path("contentLink")
                .path("expanded")
                .path("cells");

        JsonNode rateCells = columns.get(1)
                .path("contentLink")
                .path("expanded")
                .path("cells");

        if (!termCells.isArray() || !rateCells.isArray()) return;

        for (int i = 0; i < Math.min(termCells.size(), rateCells.size()); i++) {
            String termText = Jsoup.parse(termCells.get(i).asText()).text();
            String rateText = Jsoup.parse(rateCells.get(i).asText()).text();

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
        return "Skandiabanken";
    }
}