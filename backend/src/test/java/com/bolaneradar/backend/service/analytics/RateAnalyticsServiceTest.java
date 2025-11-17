package com.bolaneradar.backend.service.analytics;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Uppdaterade enhetstester för RateAnalyticsService.
 */
@ExtendWith(MockitoExtension.class)
class RateAnalyticsServiceTest {

    @Mock
    MortgageRateRepository mortgageRateRepository;

    @Mock
    BankService bankService;

    @InjectMocks
    RateAnalyticsService rateAnalyticsService;

    // Hjälpmetod
    private MortgageRate rate(Bank bank, MortgageTerm term, RateType type,
                              LocalDate date, double value) {
        return new MortgageRate(
                bank,
                term,
                type,
                BigDecimal.valueOf(value),
                date
        );
    }

    // ============================================================
    // getRateHistoryForBank()
    // ============================================================
    @Test
    void getRateHistoryForBank_filtersByDateTypeTerm_andSortsAscByDefault() {
        Bank bank = new Bank("SEB");
        bank.setId(1L);

        LocalDate d1 = LocalDate.of(2024, 1, 1);
        LocalDate d2 = LocalDate.of(2024, 1, 10);
        LocalDate d3 = LocalDate.of(2024, 1, 20);

        MortgageRate r1 = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, d1, 3.5);
        MortgageRate r2 = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, d2, 3.6);
        MortgageRate r3 = rate(bank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, d3, 3.8);

        when(bankService.getBankById(1L)).thenReturn(Optional.of(bank));
        when(mortgageRateRepository.findByBank(bank))
                .thenReturn(List.of(r3, r1, r2));

        List<MortgageRate> result = rateAnalyticsService.getRateHistoryForBank(
                1L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 15),
                null,
                RateType.LISTRATE,
                MortgageTerm.FIXED_1Y
        );

        assertEquals(2, result.size());
        assertEquals(d1, result.get(0).getEffectiveDate());
        assertEquals(d2, result.get(1).getEffectiveDate());
        assertEquals(MortgageTerm.FIXED_1Y, result.get(0).getTerm());
    }

    @Test
    void getRateHistoryForBank_sortsDesc_whenSortDescIsGiven() {
        Bank bank = new Bank("Nordea");
        bank.setId(2L);

        LocalDate d1 = LocalDate.of(2024, 2, 1);
        LocalDate d2 = LocalDate.of(2024, 2, 5);

        MortgageRate r1 = rate(bank, MortgageTerm.FIXED_1Y, RateType.AVERAGERATE, d1, 3.0);
        MortgageRate r2 = rate(bank, MortgageTerm.FIXED_1Y, RateType.AVERAGERATE, d2, 3.2);

        when(bankService.getBankById(2L)).thenReturn(Optional.of(bank));
        when(mortgageRateRepository.findByBank(bank)).thenReturn(List.of(r1, r2));

        List<MortgageRate> result = rateAnalyticsService.getRateHistoryForBank(
                2L,
                null,
                null,
                "desc",
                RateType.AVERAGERATE,
                MortgageTerm.FIXED_1Y
        );

        assertEquals(2, result.size());
        assertEquals(d2, result.get(0).getEffectiveDate());
        assertEquals(d1, result.get(1).getEffectiveDate());
    }

    // ============================================================
    // getAllBanksRateHistory()
    // ============================================================
    @Test
    void getAllBanksRateHistory_returnsMapPerBank_andDelegatesCorrectly() {

        Bank seb = new Bank("SEB");
        seb.setId(1L);

        Bank nordea = new Bank("Nordea");
        nordea.setId(2L);

        LocalDate d = LocalDate.of(2024, 3, 1);

        MortgageRate r1 = rate(seb, MortgageTerm.FIXED_1Y, RateType.LISTRATE, d, 3.5);
        MortgageRate r2 = rate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, d, 3.7);

        // Service hämtar banker här:
        when(bankService.getAllBanks()).thenReturn(List.of(seb, nordea));

        // Nya logikvägen: service kallar getBankById() i varje iteration
        when(bankService.getBankById(1L)).thenReturn(Optional.of(seb));
        when(bankService.getBankById(2L)).thenReturn(Optional.of(nordea));

        // Jepp, samma som tidigare
        when(mortgageRateRepository.findByBank(seb)).thenReturn(List.of(r1));
        when(mortgageRateRepository.findByBank(nordea)).thenReturn(List.of(r2));

        Map<String, List<MortgageRate>> result =
                rateAnalyticsService.getAllBanksRateHistory(null, null, "asc");

        assertEquals(2, result.size());
        assertEquals(1, result.get("SEB").size());
        assertEquals(1, result.get("Nordea").size());
    }

    // ============================================================
    // Trend-tester (oförändrade)
    // ============================================================

    @Test
    void getRateTrends_withExplicitFromTo_buildsTrendsBetweenTwoDates() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 2, 1);

        Bank bank = new Bank("Handelsbanken");

        MortgageRate prevRate = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, from, 3.0);
        MortgageRate latestRate = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, to, 3.4);

        when(mortgageRateRepository.findByEffectiveDate(from)).thenReturn(List.of(prevRate));
        when(mortgageRateRepository.findByEffectiveDate(to)).thenReturn(List.of(latestRate));

        List<RateTrend> trends = rateAnalyticsService.getRateTrends(from, to, RateType.LISTRATE);

        assertEquals(1, trends.size());
        RateTrend t = trends.getFirst();
        assertEquals("Handelsbanken", t.getBankName());
    }

    @Test
    void getRateTrends_forAverageRate_usesPerBankDatesAndBuildsTrends() {

        Bank seb = new Bank("SEB");

        LocalDate prevDate = LocalDate.of(2024, 4, 1);
        LocalDate latestDate = LocalDate.of(2024, 4, 15);

        MortgageRate prevRate = rate(seb, MortgageTerm.VARIABLE_3M, RateType.AVERAGERATE, prevDate, 3.1);
        MortgageRate latestRate = rate(seb, MortgageTerm.VARIABLE_3M, RateType.AVERAGERATE, latestDate, 3.5);

        when(bankService.getAllBanks()).thenReturn(List.of(seb));
        when(mortgageRateRepository.findDistinctEffectiveDatesByBankAndRateTypeDesc(seb, RateType.AVERAGERATE))
                .thenReturn(List.of(latestDate, prevDate));

        when(mortgageRateRepository.findByBankAndRateTypeAndEffectiveDate(seb, RateType.AVERAGERATE, latestDate))
                .thenReturn(List.of(latestRate));
        when(mortgageRateRepository.findByBankAndRateTypeAndEffectiveDate(seb, RateType.AVERAGERATE, prevDate))
                .thenReturn(List.of(prevRate));

        List<RateTrend> trends = rateAnalyticsService.getRateTrends(null, null, RateType.AVERAGERATE);

        assertEquals(1, trends.size());
    }

    @Test
    void getRateTrends_globalMode_usesTwoLatestDatesAndFiltersByType() {

        LocalDate prevDate = LocalDate.of(2024, 5, 1);
        LocalDate latestDate = LocalDate.of(2024, 5, 10);

        Bank seb = new Bank("SEB");

        MortgageRate prevListRate = rate(seb, MortgageTerm.FIXED_3Y, RateType.LISTRATE, prevDate, 4.0);
        MortgageRate prevAverage = rate(seb, MortgageTerm.FIXED_3Y, RateType.AVERAGERATE, prevDate, 3.8);

        MortgageRate latestListRate = rate(seb, MortgageTerm.FIXED_3Y, RateType.LISTRATE, latestDate, 4.2);
        MortgageRate latestAverage = rate(seb, MortgageTerm.FIXED_3Y, RateType.AVERAGERATE, latestDate, 4.0);

        when(mortgageRateRepository.findDistinctEffectiveDatesDesc())
                .thenReturn(List.of(latestDate, prevDate));

        when(mortgageRateRepository.findByEffectiveDate(prevDate))
                .thenReturn(List.of(prevListRate, prevAverage));
        when(mortgageRateRepository.findByEffectiveDate(latestDate))
                .thenReturn(List.of(latestListRate, latestAverage));

        List<RateTrend> trends = rateAnalyticsService.getRateTrends(null, null, RateType.LISTRATE);

        assertEquals(1, trends.size());
    }

    @Test
    void getRateTrendsInRange_buildsTrendsForConsecutiveDatesPerGroup_andSortsByChangeDesc() {

        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to = LocalDate.of(2024, 6, 10);

        Bank bank = new Bank("SBAB");

        MortgageRate r1 = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE,
                LocalDate.of(2024, 6, 1), 3.0);
        MortgageRate r2 = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE,
                LocalDate.of(2024, 6, 5), 3.3);
        MortgageRate r3 = rate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE,
                LocalDate.of(2024, 6, 9), 4.0);

        when(mortgageRateRepository.findByEffectiveDateBetween(from, to))
                .thenReturn(List.of(r2, r1, r3));

        List<RateTrend> trends = rateAnalyticsService.getRateTrendsInRange(from, to, RateType.LISTRATE);

        assertEquals(2, trends.size());
    }
}