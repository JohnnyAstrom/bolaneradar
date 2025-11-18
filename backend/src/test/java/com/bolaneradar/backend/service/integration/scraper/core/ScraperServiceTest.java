package com.bolaneradar.backend.service.integration.scraper.core;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import com.bolaneradar.backend.service.integration.EmailService;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperServiceTest {

    @Mock BankRepository bankRepository;
    @Mock MortgageRateRepository mortgageRateRepository;
    @Mock RateUpdateLogService rateUpdateLogService;
    @Mock EmailService emailService;

    private BankScraper scraperA;
    private BankScraper scraperB;
    private List<BankScraper> scrapers;

    private ScraperService service;

    @BeforeEach
    void setup() {
        scraperA = mock(BankScraper.class, withSettings().name("BankAScraper"));
        scraperB = mock(BankScraper.class, withSettings().name("BankBScraper"));

        scrapers = new ArrayList<>(List.of(scraperA, scraperB));

        service = new ScraperService(
                bankRepository,
                mortgageRateRepository,
                scrapers,
                rateUpdateLogService,
                emailService
        );
    }

    private Bank mockBank(String name) {
        Bank bank = mock(Bank.class);
        when(bank.getName()).thenReturn(name);
        return bank;
    }

    private MortgageRate mockRate(
            Bank bank,
            MortgageTerm term,
            RateType rateType,
            LocalDate date,
            BigDecimal value) {

        MortgageRate r = mock(MortgageRate.class);
        lenient().when(r.getBank()).thenReturn(bank);
        lenient().when(r.getTerm()).thenReturn(term);
        lenient().when(r.getRateType()).thenReturn(rateType);
        lenient().when(r.getEffectiveDate()).thenReturn(date);
        lenient().when(r.getRatePercent()).thenReturn(value);
        return r;
    }

    // =====================================================================
    //  1. BANK SAKNAS
    // =====================================================================
    @Test
    void runScrapeForBank_returnsFailure_whenBankNotFound() {
        when(bankRepository.findByNameIgnoreCase("Fantombank"))
                .thenReturn(Optional.empty());

        ScraperResult result = service.runScrapeForBank("Fantombank");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("Ingen bank hittades");
        verifyNoInteractions(rateUpdateLogService);
        verifyNoInteractions(mortgageRateRepository);
    }

    // =====================================================================
    //  2. SCRAPER SAKNAS
    // =====================================================================
    @Test
    void runScrapeForBank_returnsFailure_whenScraperMissing() {
        Bank bank = mockBank("OkändBank");
        when(bankRepository.findByNameIgnoreCase("OkändBank"))
                .thenReturn(Optional.of(bank));

        // Inga scrapers matchar namnet
        scrapers.clear();

        ScraperResult result = service.runScrapeForBank("OkändBank");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("Ingen scraper hittades");
        verify(rateUpdateLogService).logUpdate(
                eq(bank), eq("ScraperService"),
                eq(0), eq(false),
                contains("Ingen scraper hittades"),
                anyLong()
        );
        verifyNoInteractions(mortgageRateRepository);
    }

    // =====================================================================
    //  3. SCRAPER RETURNERAR TOM LISTA
    // =====================================================================
    @Test
    void runScrapeForBank_failure_whenScraperReturnsEmptyList() throws Exception {
        Bank bank = mockBank("BankA");
        when(bankRepository.findByNameIgnoreCase("BankA"))
                .thenReturn(Optional.of(bank));

        scrapers.clear();
        scrapers.add(scraperA);
        when(scraperA.scrapeRates(bank)).thenReturn(List.of());

        ScraperResult result = service.runScrapeForBank("BankA");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("Inga räntor hittades");

        verify(rateUpdateLogService).logUpdate(
                eq(bank), eq("ScraperService"),
                eq(0), eq(false),
                contains("Inga räntor hittades"),
                anyLong()
        );
        verify(mortgageRateRepository, never()).saveAll(any());
    }

    // =====================================================================
    //  4. LYCKAT CASE – 1 SPARAD RATE
    // =====================================================================
    @Test
    void runScrapeForBank_savesRates_onSuccess() throws Exception {
        Bank bank = mockBank("BankA");
        when(bankRepository.findByNameIgnoreCase("BankA"))
                .thenReturn(Optional.of(bank));

        MortgageRate newRate = mockRate(
                bank, MortgageTerm.VARIABLE_3M,
                RateType.AVERAGERATE,
                LocalDate.now(),
                new BigDecimal("4.25")
        );

        scrapers.clear();
        scrapers.add(scraperA);
        when(scraperA.scrapeRates(bank)).thenReturn(List.of(newRate));

        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(), eq(RateType.AVERAGERATE)
        )).thenReturn(List.of());

        ScraperResult result = service.runScrapeForBank("BankA");

        assertThat(result.success()).isTrue();
        assertThat(result.importedCount()).isEqualTo(1);

        verify(mortgageRateRepository).saveAll(any());
        verify(rateUpdateLogService)
                .logUpdate(eq(bank), eq("ScraperService"),
                        eq(1), eq(true),
                        isNull(), anyLong());
    }

    // =====================================================================
    //  5. SNITTRÄNTA – DUBLETT SKA SKIPPAS
    // =====================================================================
    @Test
    void runScrapeForBank_skipsAverageDuplicate() throws Exception {
        Bank bank = mockBank("BankA");
        when(bankRepository.findByNameIgnoreCase("BankA"))
                .thenReturn(Optional.of(bank));

        LocalDate today = LocalDate.now();
        BigDecimal val = new BigDecimal("3.50");

        MortgageRate existing = mockRate(bank,
                MortgageTerm.VARIABLE_3M,
                RateType.AVERAGERATE,
                today,
                val);

        MortgageRate incoming = mockRate(bank,
                MortgageTerm.VARIABLE_3M,
                RateType.AVERAGERATE,
                today,
                val);

        scrapers.clear();
        scrapers.add(scraperA);
        when(scraperA.scrapeRates(bank)).thenReturn(List.of(incoming));

        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(), eq(RateType.AVERAGERATE)))
                .thenReturn(List.of(existing));

        ScraperResult result = service.runScrapeForBank("BankA");

        assertThat(result.success()).isTrue();
        assertThat(result.importedCount()).isEqualTo(0);

        verify(mortgageRateRepository, never()).saveAll(any());
        verify(rateUpdateLogService).logUpdate(
                eq(bank), eq("ScraperService"),
                eq(0), eq(true),
                isNull(), anyLong());
    }

    // =====================================================================
    //  6. RATECHANGE & LASTCHANGEDDATE SÄTTS VID NYARE DATUM
    // =====================================================================
    @Test
    void runScrapeForBank_setsRateChange_onNewerValue() throws Exception {
        Bank bank = mockBank("BankA");
        when(bankRepository.findByNameIgnoreCase("BankA"))
                .thenReturn(Optional.of(bank));

        LocalDate oldDate = LocalDate.now().minusDays(1);
        LocalDate newDate = LocalDate.now();
        BigDecimal oldVal = new BigDecimal("3.10");
        BigDecimal newVal = new BigDecimal("3.30");

        MortgageRate oldRate = mockRate(bank,
                MortgageTerm.FIXED_1Y,
                RateType.LISTRATE,
                oldDate,
                oldVal);

        MortgageRate incoming = mockRate(bank,
                MortgageTerm.FIXED_1Y,
                RateType.LISTRATE,
                newDate,
                newVal);

        scrapers.clear();
        scrapers.add(scraperA);
        when(scraperA.scrapeRates(bank)).thenReturn(List.of(incoming));

        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(), eq(RateType.LISTRATE)))
                .thenReturn(List.of(oldRate));

        ScraperResult result = service.runScrapeForBank("BankA");

        assertThat(result.importedCount()).isEqualTo(1);

        verify(incoming).setRateChange(new BigDecimal("0.20"));
        verify(incoming).setLastChangedDate(newDate);
        verify(mortgageRateRepository).saveAll(any());
    }

    // =====================================================================
    //  7. SCRAPER KASTAR EXCEPTION → FAILURE
    // =====================================================================
    @Test
    void runScrapeForBank_handlesScraperException() throws Exception {
        Bank bank = mockBank("BankA");
        when(bankRepository.findByNameIgnoreCase("BankA"))
                .thenReturn(Optional.of(bank));

        scrapers.clear();
        scrapers.add(scraperA);
        when(scraperA.scrapeRates(bank)).thenThrow(new RuntimeException("Timeout"));

        ScraperResult result = service.runScrapeForBank("BankA");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("Timeout");

        verify(rateUpdateLogService).logUpdate(
                eq(bank), eq("ScraperService"),
                eq(0), eq(false),
                contains("Timeout"),
                anyLong());
    }

    // =====================================================================
    //  8. WRAPPER: scrapeSingleBank → SUCCESS
    // =====================================================================
    @Test
    void scrapeSingleBank_returnsText_onSuccess() throws Exception {
        ScraperService spyService = spy(service);

        doReturn(new ScraperResult("SEB", 3, true, null, 10))
                .when(spyService)
                .runScrapeForBank("SEB");

        String text = spyService.scrapeSingleBank("SEB");

        assertThat(text).isEqualTo("3 räntor sparade för SEB");
    }

    // =====================================================================
    //  9. WRAPPER: scrapeSingleBank → THROW
    // =====================================================================
    @Test
    void scrapeSingleBank_throws_onFailure() throws Exception {
        ScraperService spyService = spy(service);

        doReturn(new ScraperResult("Nordea", 0, false,
                "Något gick fel", 10))
                .when(spyService)
                .runScrapeForBank("Nordea");

        assertThatThrownBy(() -> spyService.scrapeSingleBank("Nordea"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Något gick fel");
    }

    // =====================================================================
    //  10. scrapeAllBanks → mail skickas om någon failar
    // =====================================================================
    @Test
    void scrapeAllBanks_sendsMail_whenAnyFails() {
        Bank b1 = mockBank("BankA");
        Bank b2 = mockBank("BankB");

        when(bankRepository.findAll()).thenReturn(List.of(b1, b2));

        ScraperService spyService = spy(service);

        doReturn(new ScraperResult("BankA", 1, true, null, 10))
                .when(spyService).runScrapeForBank("BankA");

        doReturn(new ScraperResult("BankB", 0, false, "fel", 10))
                .when(spyService).runScrapeForBank("BankB");

        spyService.scrapeAllBanks();

        verify(emailService).sendErrorNotification(
                contains("BolåneRadar"),
                contains("BankB")
        );
    }

    // =====================================================================
    //  11. scrapeAllBanks → inget mail när alla lyckas
    // =====================================================================
    @Test
    void scrapeAllBanks_noMail_whenAllSuccess() {
        Bank b1 = mockBank("BankA");
        Bank b2 = mockBank("BankB");

        when(bankRepository.findAll()).thenReturn(List.of(b1, b2));

        ScraperService spyService = spy(service);

        doReturn(new ScraperResult("BankA", 1, true, null, 10))
                .when(spyService).runScrapeForBank("BankA");

        doReturn(new ScraperResult("BankB", 1, true, null, 10))
                .when(spyService).runScrapeForBank("BankB");

        spyService.scrapeAllBanks();

        verifyNoInteractions(emailService);
    }
}