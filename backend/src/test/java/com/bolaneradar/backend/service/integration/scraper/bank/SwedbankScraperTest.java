package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SwedbankScraperTest {
    private final Bank bank = new Bank("Swedbank");

    private List<MortgageRate> scrape(String month) throws IOException {
        var list = Jsoup.parse("""
                <table><caption>Aktuella listräntor</caption>
                <thead><tr><th>Bindningstid</th><th>Snittränta</th><th>Listränta</th><th>Ändrad</th></tr></thead>
                <tbody><tr><td>3 månader</td><td>2,70 %</td><td>3,89 %</td><td>2025-01-01</td></tr></tbody></table>
                """);
        var average = Jsoup.parse("""
                <table><thead><tr><th>Bindningstid</th><th>3 månader</th><th>1 år</th><th>Banklån</th></tr></thead>
                <tbody><tr><td>%s</td><td>2,70 %%</td><td>2,92 %%</td><td>3,61 %%</td></tr>
                <tr><td>dec. 2024</td><td>3,52 %%</td><td>3,31 %%</td><td>4,14 %%</td></tr></tbody></table>
                """.formatted(month));
        try (var utils = mockStatic(ScraperUtils.class, CALLS_REAL_METHODS)) {
            utils.when(() -> ScraperUtils.fetchDocument(
                    "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor.html")).thenReturn(list);
            utils.when(() -> ScraperUtils.fetchDocument(
                    "https://www.swedbank.se/privat/boende-och-bolan/bolanerantor/historiska-genomsnittsrantor.html"))
                    .thenReturn(average);
            return new SwedbankScraper().scrapeRates(bank);
        }
    }

    @ParameterizedTest
    @CsvSource({"januari,1", "jan.,1", "februari,2", "feb.,2", "mars,3", "mar.,3",
            "april,4", "apr.,4", "maj,5", "juni,6", "jun.,6", "juli,7", "jul.,7",
            "augusti,8", "aug.,8", "september,9", "sep.,9", "sept.,9", "oktober,10",
            "okt.,10", "november,11", "nov.,11", "december,12", "dec.,12", "AUG,8"})
    void usesPublishedMonthAndYearWithoutSubtractingOrUsingCurrentMonth(String month, int number) throws Exception {
        var rates = scrape(month + " 2025");
        assertEquals(3, rates.size());
        var averages = rates.stream().filter(r -> r.getRateType() == RateType.AVERAGERATE).toList();
        assertEquals(List.of(MortgageTerm.VARIABLE_3M, MortgageTerm.FIXED_1Y),
                averages.stream().map(MortgageRate::getTerm).toList());
        assertEquals(List.of(new BigDecimal("2.70"), new BigDecimal("2.92")),
                averages.stream().map(MortgageRate::getRatePercent).toList());
        averages.forEach(rate -> {
            assertSame(bank, rate.getBank());
            assertEquals(LocalDate.of(2025, number, 1), rate.getEffectiveDate());
        });
        var listRate = rates.getFirst();
        assertEquals(RateType.LISTRATE, listRate.getRateType());
        assertEquals(MortgageTerm.VARIABLE_3M, listRate.getTerm());
        assertEquals(new BigDecimal("3.89"), listRate.getRatePercent());
        assertEquals(LocalDate.now(), listRate.getEffectiveDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {" AUG.\u00a02025 ", "aug.\u202f2025", "aug.   2025"})
    void handlesWhitespace(String month) throws Exception {
        assertEquals(LocalDate.of(2025, 8, 1), scrape(month).get(1).getEffectiveDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"okänd 2025", "aug.", "2025", "", "aug. 25", "juli 2025 extra"})
    void rejectsUnknownMonthInsteadOfGuessing(String month) {
        var error = assertThrows(IOException.class, () -> scrape(month));
        assertTrue(error.getMessage().contains("Swedbank: kunde inte tolka månad"));
    }

    @Test
    void retainsFutureDateGuard() throws Exception {
        var rates = scrape("januari " + (LocalDate.now().getYear() + 1));
        assertEquals(1, rates.size());
        assertEquals(RateType.LISTRATE, rates.getFirst().getRateType());
    }
}
