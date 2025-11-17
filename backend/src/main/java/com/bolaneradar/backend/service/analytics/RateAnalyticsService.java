package com.bolaneradar.backend.service.analytics;

import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ================================================================
 *  RATE ANALYTICS SERVICE
 * ================================================================
 *  Detta lager ansvarar för:
 *   - All affärslogik
 *   - Filtrering, sortering, grouping
 *   - Datumjämförelser och analys
 *   - Generering av historik- och trendresultat
 *
 *  Controller-lagret ska enbart skicka parametrar vidare och
 *  konvertera entiteter till DTOs.
 * ================================================================
 */
@Service
public class RateAnalyticsService {

    private final MortgageRateRepository mortgageRateRepository;
    private final BankService bankService;

    public RateAnalyticsService(
            MortgageRateRepository mortgageRateRepository,
            BankService bankService
    ) {
        this.mortgageRateRepository = mortgageRateRepository;
        this.bankService = bankService;
    }

    // ===========================================================
    // =============         HISTORIK-FLÖDE         ==============
    // ===========================================================

    /**
     * Hämtar historik för en bank, inklusive all filtrering och sortering.
     *
     * Detta motsvarar tidigare controller + service-logik, men
     * är nu helt samlad här för bästa separation av ansvar.
     */
    public List<MortgageRate> getRateHistoryForBank(
            Long bankId,
            LocalDate from,
            LocalDate to,
            String sort,
            RateType rateType,
            MortgageTerm term
    ) {

        // ===== 1. Validera bank ===============================================
        Optional<Bank> optionalBank = bankService.getBankById(bankId);
        if (optionalBank.isEmpty()) {
            return Collections.emptyList(); // Controller skickar OK([]) = korrekt beteende
        }
        Bank bank = optionalBank.get();

        // ===== 2. Hämta rådata från repository ================================
        List<MortgageRate> rates = mortgageRateRepository.findByBank(bank);

        // ===== 3. Datumintervall-filter ======================================
        if (from != null) {
            rates = rates.stream()
                    .filter(r -> !r.getEffectiveDate().isBefore(from))
                    .toList();
        }
        if (to != null) {
            rates = rates.stream()
                    .filter(r -> !r.getEffectiveDate().isAfter(to))
                    .toList();
        }

        // ===== 4. Typ-filter ==================================================
        if (rateType != null) {
            rates = rates.stream()
                    .filter(r -> r.getRateType() == rateType)
                    .toList();
        }

        // ===== 5. Term-filter =================================================
        if (term != null) {
            rates = rates.stream()
                    .filter(r -> r.getTerm() == term)
                    .toList();
        }

        // ===== 6. Sortering (asc/desc på effectiveDate) =======================
        Comparator<MortgageRate> comparator = Comparator.comparing(MortgageRate::getEffectiveDate);

        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }

        return rates.stream().sorted(comparator).toList();
    }

    /**
     * Hämtar historik för alla banker, grupperat per banknamn.
     */
    public Map<String, List<MortgageRate>> getAllBanksRateHistory(
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        List<Bank> banks = bankService.getAllBanks();

        return banks.stream()
                .collect(Collectors.toMap(
                        Bank::getName,
                        bank -> getRateHistoryForBank(bank.getId(), from, to, sort, null, null)
                ));
    }

    // ===========================================================
    // =============          TREND-FLÖDE          ===============
    // ===========================================================

    /**
     * Beräknar trender mellan två snapshot-datum eller de två senaste.
     */
    public List<RateTrend> getRateTrends(LocalDate from, LocalDate to, RateType type) {

        // ===== A. Exakt intervall (from + to angivet) ==========================
        if (from != null && to != null) {
            List<MortgageRate> latest = mortgageRateRepository.findByEffectiveDate(to);
            List<MortgageRate> prev = mortgageRateRepository.findByEffectiveDate(from);

            if (type != null) {
                latest = filterByType(latest, type);
                prev = filterByType(prev, type);
            }

            return buildTrendsFromTwoSnapshots(prev, latest, from, to);
        }

        // ===== B. Per bank (AVERAGERATE) ======================================
        if (type == RateType.AVERAGERATE) {
            List<RateTrend> out = new ArrayList<>();

            for (Bank bank : bankService.getAllBanks()) {

                List<LocalDate> dates =
                        mortgageRateRepository.findDistinctEffectiveDatesByBankAndRateTypeDesc(bank, type);

                if (dates.size() < 2) continue;

                LocalDate latestDate = dates.get(0);
                LocalDate prevDate = dates.get(1);

                List<MortgageRate> latest =
                        mortgageRateRepository.findByBankAndRateTypeAndEffectiveDate(bank, type, latestDate);

                List<MortgageRate> prev =
                        mortgageRateRepository.findByBankAndRateTypeAndEffectiveDate(bank, type, prevDate);

                out.addAll(buildTrendsFromTwoSnapshots(prev, latest, prevDate, latestDate));
            }

            sortTrends(out);
            return out;
        }

        // ===== C. Globala senaste två datum ===================================
        List<LocalDate> allDates = mortgageRateRepository.findDistinctEffectiveDatesDesc();
        if (allDates.size() < 2) return Collections.emptyList();

        LocalDate toDate = allDates.get(0);
        LocalDate fromDate = allDates.get(1);

        List<MortgageRate> latest = mortgageRateRepository.findByEffectiveDate(toDate);
        List<MortgageRate> prev = mortgageRateRepository.findByEffectiveDate(fromDate);

        if (type != null) {
            latest = filterByType(latest, type);
            prev = filterByType(prev, type);
        }

        List<RateTrend> trends = buildTrendsFromTwoSnapshots(prev, latest, fromDate, toDate);
        sortTrends(trends);
        return trends;
    }

    /**
     * Trender för ALLA datapunkter i ett intervall.
     */
    public List<RateTrend> getRateTrendsInRange(LocalDate from, LocalDate to, RateType type) {

        List<MortgageRate> rates =
                mortgageRateRepository.findByEffectiveDateBetween(from, to);

        if (type != null) {
            rates = rates.stream()
                    .filter(r -> r.getRateType() == type)
                    .toList();
        }

        // Gruppera per bank + term + type
        Map<String, List<MortgageRate>> grouped = rates.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getBank().getName() + "_" +
                                r.getTerm() + "_" +
                                r.getRateType()
                ));

        List<RateTrend> allTrends = new ArrayList<>();

        for (List<MortgageRate> group : grouped.values()) {
            group.sort(Comparator.comparing(MortgageRate::getEffectiveDate));

            for (int i = 0; i < group.size() - 1; i++) {
                MortgageRate prev = group.get(i);
                MortgageRate next = group.get(i + 1);

                allTrends.add(new RateTrend(
                        prev.getBank().getName(),
                        prev.getTerm().name(),
                        prev.getRateType().name(),
                        prev.getRatePercent().doubleValue(),
                        next.getRatePercent().doubleValue(),
                        prev.getEffectiveDate(),
                        next.getEffectiveDate()
                ));
            }
        }

        sortTrends(allTrends);
        return allTrends;
    }

    // ===========================================================
    // =============      INTERNA HJÄLPMETODER      ==============
    // ===========================================================

    private List<MortgageRate> filterByType(List<MortgageRate> list, RateType type) {
        return list.stream().filter(r -> r.getRateType() == type).toList();
    }

    private List<RateTrend> buildTrendsFromTwoSnapshots(
            List<MortgageRate> previousRates,
            List<MortgageRate> latestRates,
            LocalDate from,
            LocalDate to
    ) {
        Map<String, Double> prevMap = previousRates.stream()
                .collect(Collectors.toMap(
                        r -> r.getBank().getName() + "_" + r.getTerm() + "_" + r.getRateType(),
                        r -> r.getRatePercent().doubleValue(),
                        (a, b) -> b
                ));

        List<RateTrend> out = new ArrayList<>();

        for (MortgageRate rate : latestRates) {
            String key =
                    rate.getBank().getName() + "_" +
                            rate.getTerm() + "_" +
                            rate.getRateType();

            Double prev = prevMap.get(key);
            if (prev != null) {
                out.add(new RateTrend(
                        rate.getBank().getName(),
                        rate.getTerm().name(),
                        rate.getRateType().name(),
                        prev,
                        rate.getRatePercent().doubleValue(),
                        from,
                        to
                ));
            }
        }
        return out;
    }

    private void sortTrends(List<RateTrend> trends) {
        trends.sort(
                Comparator.comparing(RateTrend::getBankName)
                        .thenComparing(RateTrend::getTerm)
                        .thenComparing(RateTrend::getRateType)
                        .thenComparing(RateTrend::getChange, Comparator.reverseOrder())
        );
    }
}