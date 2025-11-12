package com.bolaneradar.backend.service.integration.scraper;

import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.RateUpdateLogService;
import com.bolaneradar.backend.service.integration.EmailService;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import com.bolaneradar.backend.service.integration.scraper.core.ScrapeResult;
import com.bolaneradar.backend.service.integration.scraper.core.ScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperServiceTest {

    @Mock BankRepository bankRepository;
    @Mock MortgageRateRepository mortgageRateRepository;
    @Mock RateUpdateLogService rateUpdateLogService;
    @Mock EmailService emailService;

    // Namngivna mock-scrapers (identifieras via getClass().getSimpleName() i servicen)
    private BankScraper swedbankScraper;
    private BankScraper nordeaScraper;
    private BankScraper sebScraper;
    private BankScraper handelsbankenScraper;
    private BankScraper sbabScraper;

    // Den lista vi ger till service – görs MUTERBAR så vi kan justera per test
    private List<BankScraper> scrapers;

    private ScraperService service;

    @BeforeEach
    void setup() {
        swedbankScraper      = makeNamedMockScraper("SwedbankScraper");
        nordeaScraper        = makeNamedMockScraper("NordeaScraper");
        sebScraper           = makeNamedMockScraper("SEBScraper");
        handelsbankenScraper = makeNamedMockScraper("HandelsbankenScraper");
        sbabScraper          = makeNamedMockScraper("SBABScraper");

        // Viktigt: muterbar lista (service behåller referensen)
        scrapers = new ArrayList<>(List.of(
                swedbankScraper, nordeaScraper, sebScraper, handelsbankenScraper, sbabScraper
        ));

        service = new ScraperService(
                bankRepository,
                mortgageRateRepository,
                scrapers,
                rateUpdateLogService,
                emailService
        );
    }

    // ===== Helpers =====

    private BankScraper makeNamedMockScraper(String name) {
        // Endast namnge – stubba beteende per test (undviker UnnecessaryStubbing)
        return mock(BankScraper.class, withSettings().name(name));
    }

    private Bank mockBank(String name) {
        Bank bank = mock(Bank.class);
        when(bank.getName()).thenReturn(name);
        return bank;
    }

    private MortgageRate mockRate(Bank bank, MortgageTerm term, RateType rateType,
                                  LocalDate date, BigDecimal percent) {
        MortgageRate r = mock(MortgageRate.class);
        lenient().when(r.getBank()).thenReturn(bank);
        lenient().when(r.getTerm()).thenReturn(term);
        lenient().when(r.getRateType()).thenReturn(rateType);
        lenient().when(r.getEffectiveDate()).thenReturn(date);
        lenient().when(r.getRatePercent()).thenReturn(percent);
        return r;
    }

    private static <T> List<T> iterableToList(Iterable<T> it) {
        List<T> out = new ArrayList<>();
        Iterator<T> iter = it.iterator();
        while (iter.hasNext()) out.add(iter.next());
        return out;
    }

    // ==========================================================
    // 1) Bank saknas → ska kasta Exception, ingen loggning sker
    // ==========================================================
    @Test
    void scrapeSingleBank_throws_whenBankNotFound() {
        when(bankRepository.findByNameIgnoreCase("Fantombank")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.scrapeSingleBank("Fantombank"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Ingen bank hittades med namn: Fantombank");

        verify(rateUpdateLogService, never())
                .logUpdate(any(), anyString(), anyInt(), anyBoolean(), any(), anyLong());
        verifyNoInteractions(mortgageRateRepository);
    }

    // ==========================================================
    // 2) Scraper saknas → Exception + failure loggas
    // ==========================================================
    @Test
    void scrapeSingleBank_throws_andLogs_whenNoScraperFound() {
        Bank bank = mockBank("OkändBank"); // matchar ingen av våra namngivna scrapers
        when(bankRepository.findByNameIgnoreCase("OkändBank")).thenReturn(Optional.of(bank));

        assertThatThrownBy(() -> service.scrapeSingleBank("OkändBank"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Ingen scraper hittades");

        verify(rateUpdateLogService, times(1))
                .logUpdate(eq(bank), eq("ScraperService"), eq(0), eq(false),
                        contains("Ingen scraper hittades"), anyLong());
        verifyNoInteractions(mortgageRateRepository);
    }

    // ==========================================================
    // 3) Scraper returnerar tom lista → Exception + failure loggas
    // ==========================================================
    @Test
    void scrapeSingleBank_throws_whenScraperReturnsEmptyRates() throws Exception {
        Bank bank = mockBank("Nordea");
        when(bankRepository.findByNameIgnoreCase("Nordea")).thenReturn(Optional.of(bank));

        // Se till att service hittar ENDAST NordeaScraper och att den ger tomt resultat
        scrapers.clear();
        scrapers.add(nordeaScraper);
        doReturn(List.of()).when(nordeaScraper).scrapeRates(any(Bank.class));

        assertThatThrownBy(() -> service.scrapeSingleBank("Nordea"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Inga räntor hittades");

        verify(rateUpdateLogService, times(1))
                .logUpdate(eq(bank), eq("ScraperService"), eq(0), eq(false),
                        contains("Inga räntor hittades"), anyLong());
        verify(mortgageRateRepository, never()).saveAll(any());
    }

    // ==========================================================
    // 4) Lyckat case → sparar 1 rate, returnerar text, loggar success
    // ==========================================================
    @Test
    void scrapeSingleBank_savesRates_andReturnsMessage_onSuccess() throws Exception {
        Bank bank = mockBank("SEB");
        when(bankRepository.findByNameIgnoreCase("SEB")).thenReturn(Optional.of(bank));

        MortgageRate newRate = mockRate(
                bank, MortgageTerm.VARIABLE_3M, RateType.AVERAGERATE, LocalDate.now(), new BigDecimal("4.25")
        );

        scrapers.clear();
        scrapers.add(sebScraper);
        doReturn(List.of(newRate)).when(sebScraper).scrapeRates(eq(bank));

        // Inga tidigare rates
        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(MortgageTerm.class), eq(RateType.AVERAGERATE))
        ).thenReturn(List.of());

        String msg = service.scrapeSingleBank("SEB");
        assertThat(msg).isEqualTo("1 räntor sparade för SEB");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<MortgageRate>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(mortgageRateRepository, times(1)).saveAll(captor.capture());
        List<MortgageRate> saved = iterableToList(captor.getValue());
        assertThat(saved).hasSize(1).contains(newRate);

        verify(rateUpdateLogService, times(1))
                .logUpdate(eq(bank), eq("ScraperService"), eq(1), eq(true), isNull(), anyLong());
    }

    // ==========================================================
    // 5) AVERAGERATE-dublett (samma datum & värde) → SKIPPA save
    //    → success=true, importedCount=0
    // ==========================================================
    @Test
    void scrapeSingleBank_skipsAverageDuplicate_andStillSuccessWithZeroSaved() throws Exception {
        Bank bank = mockBank("Handelsbanken");
        when(bankRepository.findByNameIgnoreCase("Handelsbanken")).thenReturn(Optional.of(bank));

        LocalDate today = LocalDate.now();
        BigDecimal same = new BigDecimal("3.50");

        MortgageRate latest   = mockRate(bank, MortgageTerm.VARIABLE_3M, RateType.AVERAGERATE, today, same);
        MortgageRate incoming = mockRate(bank, MortgageTerm.VARIABLE_3M, RateType.AVERAGERATE, today, same);

        scrapers.clear();
        scrapers.add(handelsbankenScraper);
        doReturn(List.of(incoming)).when(handelsbankenScraper).scrapeRates(eq(bank));

        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(MortgageTerm.class), eq(RateType.AVERAGERATE))
        ).thenReturn(List.of(latest));

        String msg = service.scrapeSingleBank("Handelsbanken");

        verify(mortgageRateRepository, never()).saveAll(any());
        assertThat(msg).isEqualTo("0 räntor sparade för Handelsbanken");

        verify(rateUpdateLogService, times(1))
                .logUpdate(eq(bank), eq("ScraperService"), eq(0), eq(true), isNull(), anyLong());
    }

    // ==========================================================
    // 6) Nyare datum & ändrat värde → rateChange + lastChangedDate sätts
    // ==========================================================
    @Test
    void scrapeSingleBank_setsRateChange_andLastChangedDate_onNewerDifferentValue() throws Exception {
        Bank bank = mockBank("SBAB");
        when(bankRepository.findByNameIgnoreCase("SBAB")).thenReturn(Optional.of(bank));

        LocalDate oldDate = LocalDate.now().minusDays(1);
        LocalDate newDate = LocalDate.now();
        BigDecimal oldVal = new BigDecimal("3.10");
        BigDecimal newVal = new BigDecimal("3.30");

        MortgageRate latest   = mockRate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, oldDate, oldVal);
        MortgageRate incoming = mockRate(bank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, newDate, newVal);

        scrapers.clear();
        scrapers.add(sbabScraper);
        doReturn(List.of(incoming)).when(sbabScraper).scrapeRates(eq(bank));

        // OBS: LISTRATE här (matchar det som servicen frågar på för incoming)
        when(mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                eq(bank), any(MortgageTerm.class), eq(RateType.LISTRATE))
        ).thenReturn(List.of(latest));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<MortgageRate>> captor = ArgumentCaptor.forClass(Iterable.class);

        String msg = service.scrapeSingleBank("SBAB");

        verify(mortgageRateRepository).saveAll(captor.capture());
        List<MortgageRate> saved = iterableToList(captor.getValue());
        assertThat(saved).hasSize(1).contains(incoming);

        // Verifiera att setters anropats korrekt
        verify(incoming, times(1)).setRateChange(new BigDecimal("0.20"));
        verify(incoming, times(1)).setLastChangedDate(newDate);

        assertThat(msg).isEqualTo("1 räntor sparade för SBAB");
        verify(rateUpdateLogService)
                .logUpdate(eq(bank), eq("ScraperService"), eq(1), eq(true), isNull(), anyLong());
    }

    // ==========================================================
    // 7) scrapeAllBanks() – om någon bank failar → e-post skickas
    // ==========================================================
    @Test
    void scrapeAllBanks_sendsEmail_whenAnyBankFails() throws Exception {
        Bank b1 = mockBank("Bank A");
        Bank b2 = mockBank("Bank B");
        when(bankRepository.findAll()).thenReturn(List.of(b1, b2));

        // Kör via spy för att styra utfallet per bank utan att gå hela vägen via scraping
        ScraperService spyService = Mockito.spy(service);

        doReturn(new ScrapeResult("Bank A", 1, true, null, 10))
                .when(spyService).scrapeSingleBankResult("Bank A");
        doReturn(new ScrapeResult("Bank B", 0, false, "Timeout", 20))
                .when(spyService).scrapeSingleBankResult("Bank B");

        spyService.scrapeAllBanks();

        verify(emailService, times(1)).sendErrorNotification(
                contains("BolåneRadar"),
                contains("Följande banker misslyckades")
        );
    }
}