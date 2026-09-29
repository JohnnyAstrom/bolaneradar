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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class LandshypotekBankScraper implements BankScraper {

    private static final Logger log = LoggerFactory.getLogger(LandshypotekBankScraper.class);
    private static final Pattern MONTH = Pattern.compile(
            "(?iu)\\b(januari|februari|mars|april|maj|juni|juli|augusti|september|oktober|november|december)\\s+(20\\d{2})\\b");
    private static final DateTimeFormatter MONTH_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("MMMM uuuu").toFormatter(Locale.forLanguageTag("sv-SE"));

    private static final String URL =
            "https://www.landshypotek.se/lana-till-bostad/bolanerantor/";

    @Override
    public String getBankName() {
        return "Landshypotek Bank";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        Document doc = ScraperUtils.fetchDocument(URL);
        return parseRates(doc, bank, LocalDate.now());
    }

    // Separat från hämtning och lagring för verifiering mot sparad HTML.
    List<MortgageRate> parseRates(Document doc, Bank bank, LocalDate today) throws IOException {
        log.debug("Landshypotek Bank: url={}, tables={}", doc.location(), doc.select("table").size());
        List<MortgageRate> rates = new ArrayList<>();
        Element listTable = findTable(doc, RateType.LISTRATE);
        Element averageTable = findTable(doc, RateType.AVERAGERATE);
        LocalDate averageDate = averageDate(averageTable, today);
        extractSimpleTable(bank, listTable, RateType.LISTRATE, today, rates);
        int listCount = rates.size();
        extractSimpleTable(bank, averageTable, RateType.AVERAGERATE, averageDate, rates);
        log.info("Landshypotek Bank: LISTRATE={}, AVERAGERATE={}, average_effective_date={}",
                listCount, rates.size() - listCount, averageDate);
        for (MortgageRate rate : rates) {
            log.debug("bank={}, rate_type={}, term={}, rate_percent={}, effective_date={}",
                    getBankName(), rate.getRateType(), rate.getTerm(), rate.getRatePercent(), rate.getEffectiveDate());
        }
        ScraperUtils.logResult(getBankName(), rates.size());
        return rates;
    }

    private Element findTable(Document doc, RateType type) throws IOException {
        List<Element> matches = doc.select("table").stream().filter(table -> {
            String caption = table.select("caption").text().toLowerCase(Locale.ROOT);
            return type == RateType.LISTRATE
                    ? caption.startsWith("listräntor") && caption.contains("bolån")
                    : caption.startsWith("snitträntor") && caption.contains("bolån")
                        && caption.contains("senaste månaden");
        }).toList();
        if (matches.size() != 1) {
            throw invalid("förväntade en " + type + "-tabell, hittade " + matches.size()
                    + "; url=" + doc.location() + "; captions=" + doc.select("caption").eachText());
        }
        return matches.getFirst();
    }

    private LocalDate averageDate(Element table, LocalDate today) throws IOException {
        Element section = table.closest("section");
        if (section != null) {
            for (Element heading : section.select("h2, h3, h4")) {
                var matcher = MONTH.matcher(heading.text());
                if (!matcher.find()) continue;
                try {
                    YearMonth month = YearMonth.parse(matcher.group(1) + " " + matcher.group(2), MONTH_FORMAT);
                    if (month.isAfter(YearMonth.from(today))) {
                        throw invalid("framtida snitträntemånad: " + month);
                    }
                    return month.atDay(1);
                } catch (DateTimeParseException e) {
                    throw invalid("ogiltig snitträntemånad: " + heading.text());
                }
            }
        }
        throw invalid("saknar svensk månad och år i snitträntetabellens rubrik");
    }

    private IOException invalid(String message) {
        log.error("Landshypotek Bank: {}", message);
        return new IOException("Landshypotek Bank: " + message);
    }

    /**
     * Matchar bindningstid och nominell ränta via rubrikerna, inte kolumnordningen.
     */
    private void extractSimpleTable(
            Bank bank,
            Element table,
            RateType rateType,
            LocalDate effectiveDate,
            List<MortgageRate> out
    ) throws IOException {
        Elements headers = table.select("thead tr").first() == null
                ? new Elements() : table.select("thead tr").first().select("th, td");
        int termColumn = -1;
        int rateColumn = -1;
        String rateHeader = rateType == RateType.LISTRATE ? "ränta" : "snittränta";
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).text().toLowerCase(Locale.ROOT).trim();
            if (header.equals("bindningstid")) termColumn = i;
            if (header.equals(rateHeader)) rateColumn = i;
        }
        if (termColumn < 0 || rateColumn < 0) {
            throw invalid("saknar kolumner för bindningstid/" + rateHeader + " i " + rateType);
        }
        Elements rows = table.select("tbody tr");
        if (rows.isEmpty()) {
            rows = table.select("tr");
        }

        Set<MortgageTerm> terms = new HashSet<>();
        int initialSize = out.size();
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.isEmpty()) continue;
            if (cols.size() <= Math.max(termColumn, rateColumn)) {
                throw invalid("för få kolumner i " + rateType + ": " + row.text());
            }

            String termText = cols.get(termColumn).text();
            String rateText = cols.get(rateColumn).text();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            if (rateType == RateType.AVERAGERATE && rateText.equalsIgnoreCase("n/a")) {
                log.debug("Landshypotek Bank: hoppar över opublicerad snittränta, term={}, value={}", termText, rateText);
                continue;
            }
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term == null || rate == null || rate.signum() <= 0 || rate.compareTo(new BigDecimal("100")) >= 0) {
                throw invalid("kan inte tolka " + rateType + ": term=" + termText + ", rate=" + rateText);
            }
            if (!terms.add(term)) throw invalid("dubbel bindningstid i " + rateType + ": " + term);

            out.add(new MortgageRate(
                    bank,
                    term,
                    rateType,
                    rate,
                    effectiveDate
            ));
        }
        if (out.size() == initialSize) throw invalid("inga publicerade räntor i " + rateType);
    }

    @Override
    public String toString() {
        return "Landshypotek Bank";
    }
}
