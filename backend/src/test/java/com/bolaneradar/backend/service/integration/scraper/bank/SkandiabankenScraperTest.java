package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkandiabankenScraperTest {
    private final Bank bank = new Bank("Skandiabanken");
    private static final String URL = "https://www.skandia.se/lana/bolan/bolanerantor";

    private Map<String, Object> expanded(Map<String, Object> block) {
        return Map.of("contentLink", Map.of("expanded", block));
    }

    private Map<String, Object> table(String name, String ingress, List<String> values) {
        return expanded(Map.of("name", name, "ingress", ingress, "columns", List.of(
                expanded(Map.of("cells", List.of("<p>3 m&aring;n</p>", "1 år", "2 år", "3 år", "5 år"))),
                expanded(Map.of("cells", values)))));
    }

    // Same table/column nesting, CMS name and rates as the page inspected on 2026-09-29.
    private List<MortgageRate> scrape(String ingress) throws Exception {
        String json = new ObjectMapper().writeValueAsString(Map.of("main", List.of(
                table("Listräntor", "Ändrade september 2026", List.of("3,55 %", "4,27 %", "4,52 %", "4,64 %", "4,78 %")),
                expanded(Map.of("name", "Lägst snittränta", "text", "Informationsblock utan räntetabell")),
                table("Snitträntor", ingress, List.of("2,58 %", "3,01 %", "3,18 %", "3,31 %", "3,66 %")))));
        var doc = Jsoup.parse("<script>SKB.pageContent = " + json + ";</script>");
        try (var utils = mockStatic(ScraperUtils.class, CALLS_REAL_METHODS)) {
            utils.when(() -> ScraperUtils.fetchDocument(URL)).thenReturn(doc);
            return new SkandiabankenScraper().scrapeRates(bank);
        }
    }

    @ParameterizedTest
    @CsvSource({"januari,1", "februari,2", "mars,3", "april,4", "maj,5", "juni,6",
            "juli,7", "augusti,8", "september,9", "oktober,10", "november,11", "december,12"})
    void usesPublishedMonthAndYearIncludingDelayedPublicationAndYearBoundaries(String month, int number) throws Exception {
        var rates = scrape("<p>V&aring;ra snittr&auml;ntor.</p><p>Snittr&auml;ntor " + month + " 2025:&nbsp;</p>");
        assertEquals(10, rates.size());
        var averages = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).toList();
        var lists = rates.stream().filter(r -> r.getRateType() == RateType.LISTRATE).toList();
        var terms = List.of(MortgageTerm.VARIABLE_3M, MortgageTerm.FIXED_1Y, MortgageTerm.FIXED_2Y,
                MortgageTerm.FIXED_3Y, MortgageTerm.FIXED_5Y);
        assertEquals(terms, averages.stream().map(MortgageRate::getTerm).toList());
        assertEquals(terms, lists.stream().map(MortgageRate::getTerm).toList());
        assertEquals(List.of("2.58", "3.01", "3.18", "3.31", "3.66"),
                averages.stream().map(r -> r.getRatePercent().toPlainString()).toList());
        assertEquals(List.of("3.55", "4.27", "4.52", "4.64", "4.78"),
                lists.stream().map(r -> r.getRatePercent().toPlainString()).toList());
        averages.forEach(r -> assertEquals(LocalDate.of(2025, number, 1), r.getEffectiveDate()));
        lists.forEach(r -> assertEquals(LocalDate.now(), r.getEffectiveDate()));
        rates.forEach(r -> assertSame(bank, r.getBank()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Snittr&auml;ntor AUGUSTI&nbsp;2025:", "Snitträntor <strong>augusti</strong> 2025",
            "Snitträntor augusti\u202f2025"})
    void handlesHtmlEntitiesCaseAndWhitespace(String ingress) throws Exception {
        assertEquals(LocalDate.of(2025, 8, 1), scrape(ingress).get(5).getEffectiveDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Snitträntor", "Snitträntor augusti", "Snitträntor okänd 2025",
            "Snitträntor augusti 2025 och juli 2025"})
    void rejectsMissingOrAmbiguousMonthInsteadOfReturningMisdatedRates(String ingress) {
        var error = assertThrows(IOException.class, () -> scrape(ingress));
        assertTrue(error.getMessage().startsWith("Skandiabanken:"));
    }

    @Test
    void rejectsFutureMonth() {
        assertThrows(IOException.class, () -> scrape("Snitträntor januari " + (LocalDate.now().getYear() + 1)));
    }

    @Test
    @EnabledIfSystemProperty(named = "skandia.live", matches = "true")
    void liveDryRun() throws Exception {
        var rates = new SkandiabankenScraper().scrapeRates(bank);
        for (var type : RateType.values()) {
            assertTrue(rates.stream().anyMatch(r -> r.getRateType() == type));
        }
        assertEquals(rates.size(), rates.stream().map(r -> r.getRateType() + ":" + r.getTerm()).distinct().count());
        for (var r : rates) {
            assertFalse(r.getEffectiveDate().isAfter(LocalDate.now()));
            System.out.printf("bank=%s rate_type=%s term=%s rate_percent=%s effective_date=%s%n",
                    bank.getName(), r.getRateType(), r.getTerm(), r.getRatePercent(), r.getEffectiveDate());
        }
    }
}
