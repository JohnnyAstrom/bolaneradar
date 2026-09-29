package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LandshypotekBankScraperTest {
    private final Bank bank = new Bank("Landshypotek Bank");
    private final LandshypotekBankScraper scraper = new LandshypotekBankScraper();
    private final LocalDate today = LocalDate.of(2026, 9, 29);
    private Document fixture() throws Exception {
        try (var input = getClass().getResourceAsStream("/scraper/landshypotek-2026-09-29.html")) {
            return Jsoup.parse(input, "UTF-8", "https://www.landshypotek.se/lana-till-bostad/bolanerantor/");
        }
    }

    @Test
    void mapsBothTypesFromServerHtmlWithoutJavascript() throws Exception {
        Document doc = fixture();
        assertNull(doc.getElementById("anchor-2607"));
        assertNull(doc.getElementById("anchor-2595"));
        assertEquals(9, doc.select("table").size());
        assertEquals(10, assertSnapshot(scraper.parseRates(doc, bank, today)).size());
    }

    private List<MortgageRate> assertSnapshot(List<MortgageRate> rates) {
        var list = rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).toList();
        var average = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).toList();
        assertEquals(List.of("VARIABLE_3M", "FIXED_1Y", "FIXED_2Y", "FIXED_3Y", "FIXED_4Y", "FIXED_5Y"),
                list.stream().map(r -> r.getTerm().name()).toList());
        assertEquals(List.of("3.09", "3.74", "4.05", "4.15", "4.25", "4.35"),
                list.stream().map(r -> r.getRatePercent().toPlainString()).toList());
        assertEquals(List.of("VARIABLE_3M", "FIXED_1Y", "FIXED_2Y", "FIXED_3Y"),
                average.stream().map(r -> r.getTerm().name()).toList());
        assertEquals(List.of("2.61", "3.10", "3.27", "3.34"),
                average.stream().map(r -> r.getRatePercent().toPlainString()).toList());
        list.forEach(r -> assertEquals(today, r.getEffectiveDate()));
        average.forEach(r -> assertEquals(LocalDate.of(2026, 8, 1), r.getEffectiveDate()));
        rates.forEach(r -> assertSame(bank, r.getBank()));
        return rates;
    }

    @Test
    void ignoresCmsIdsAndColumnOrder() throws Exception {
        Document doc = fixture();
        doc.select("[id]").forEach(e -> e.removeAttr("id"));
        doc.select("table tr").forEach(row -> {
            var cells = row.select("th, td");
            if (cells.size() > 1) {
                var first = cells.first();
                first.remove();
                row.appendChild(first);
            }
        });
        assertSnapshot(scraper.parseRates(doc, bank, today));
    }

    @ParameterizedTest
    @CsvSource({"Januari,1", "Februari,2", "Mars,3", "April,4", "Maj,5", "Juni,6",
            "Juli,7", "Augusti,8", "September,9", "Oktober,10", "November,11", "December,12"})
    void usesPublishedMonthEvenWhenPublicationIsDelayed(String month, int number) throws Exception {
        Document doc = fixture();
        doc.select("#anchor-16676 h3").first().text("Snitträntor " + month + " 2025");
        var rates = scraper.parseRates(doc, bank, LocalDate.of(2026, 1, 2));
        rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE)
                .forEach(r -> assertEquals(LocalDate.of(2025, number, 1), r.getEffectiveDate()));
    }

    @Test
    void rejectsMissingOrAmbiguousTablesRatherThanReturningPartialData() throws Exception {
        for (String id : List.of("anchor-16667", "anchor-16676")) {
            Document missing = fixture();
            missing.getElementById(id).remove();
            assertThrows(IOException.class, () -> scraper.parseRates(missing, bank, today));
            Document duplicate = fixture();
            duplicate.body().appendChild(duplicate.getElementById(id).clone());
            assertThrows(IOException.class, () -> scraper.parseRates(duplicate, bank, today));
        }
    }

    @Test
    void rejectsUnknownAndFutureMonthsInsteadOfGuessing() throws Exception {
        for (String heading : List.of("Snitträntor", "Snitträntor okänd 2026", "Snitträntor Oktober 2026")) {
            Document doc = fixture();
            doc.select("#anchor-16676 h3").first().text(heading);
            assertThrows(IOException.class, () -> scraper.parseRates(doc, bank, today));
        }
    }

    @Test
    void rejectsMalformedRatesTermsHeadersAndDuplicateTerms() throws Exception {
        for (String id : List.of("anchor-16667", "anchor-16676")) {
            Document badRate = fixture();
            badRate.select("#" + id + " tbody tr").first().select("td").get(1).text("okänd");
            assertThrows(IOException.class, () -> scraper.parseRates(badRate, bank, today));
            Document badTerm = fixture();
            badTerm.select("#" + id + " tbody td").first().text("okänd");
            assertThrows(IOException.class, () -> scraper.parseRates(badTerm, bank, today));
            Document badHeader = fixture();
            badHeader.select("#" + id + " thead th").get(1).text("Effektiv ränta");
            assertThrows(IOException.class, () -> scraper.parseRates(badHeader, bank, today));
            Document duplicate = fixture();
            var body = duplicate.select("#" + id + " tbody").first();
            body.appendChild(body.select("tr").first().clone());
            assertThrows(IOException.class, () -> scraper.parseRates(duplicate, bank, today));
        }
    }

    @Test
    void scrapeEntryPointUsesTheSameParser() throws Exception {
        Document doc = fixture();
        try (var utils = mockStatic(ScraperUtils.class, CALLS_REAL_METHODS)) {
            utils.when(() -> ScraperUtils.fetchDocument("https://www.landshypotek.se/lana-till-bostad/bolanerantor/"))
                    .thenReturn(doc);
            var rates = scraper.scrapeRates(bank);
            assertEquals(6, rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).count());
            assertEquals(4, rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).count());
        }
    }

    // Explicit opt-in: uses the production Jsoup fetcher, never starts Spring or connects to a DB.
    @Test
    @EnabledIfSystemProperty(named = "landshypotek.live", matches = "true")
    void liveDryRun() throws Exception {
        var rates = scraper.scrapeRates(bank);
        assertTrue(rates.stream().anyMatch(r -> r.getRateType() == RateType.LISTRATE));
        assertTrue(rates.stream().anyMatch(r -> r.getRateType() == RateType.AVERAGERATE));
        assertEquals(rates.size(), rates.stream().map(r -> r.getRateType() + ":" + r.getTerm()).distinct().count());
        for (var rate : rates) {
            assertTrue(rate.getRatePercent().compareTo(BigDecimal.ZERO) > 0);
            assertFalse(rate.getEffectiveDate().isAfter(LocalDate.now()));
            System.out.printf("bank=%s, rate_type=%s, term=%s, rate_percent=%s, effective_date=%s%n",
                    bank.getName(), rate.getRateType(), rate.getTerm(), rate.getRatePercent(), rate.getEffectiveDate());
        }
    }
}
