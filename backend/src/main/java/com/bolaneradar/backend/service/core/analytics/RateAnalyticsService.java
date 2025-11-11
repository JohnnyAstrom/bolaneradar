package com.bolaneradar.backend.service.core.analytics;

import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.analytics.RateTrend;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-lager som ansvarar för analys, historik och trendberäkningar
 * av bolåneräntor mellan olika banker och datum.
 *
 * Arbetar enbart med entiteter. Mapping till DTO sker i controller-lagret.
 */
@Service
public class RateAnalyticsService {

    private final MortgageRateRepository mortgageRateRepository;
    private final BankService bankService;

    public RateAnalyticsService(MortgageRateRepository mortgageRateRepository, BankService bankService) {
        this.mortgageRateRepository = mortgageRateRepository;
        this.bankService = bankService;
    }

    // ===========================================================
    // =============       HISTORIK & FILTRERING     =============
    // ===========================================================

    /**
     * Hämtar historiska bolåneräntor för en viss bank,
     * grupperat per bindningstid (term) och räntetyp (rateType).
     *
     * Möjlighet finns att filtrera på term, rateType och datumintervall.
     */
    public List<MortgageRate> getRateHistoryForBank(
            Bank bank,
            LocalDate from,
            LocalDate to,
            String sort,
            RateType rateType,
            MortgageTerm term
    ) {
        List<MortgageRate> rates = mortgageRateRepository.findByBank(bank);

        // Filtrera på datumintervall
        if (from != null) {
            rates = rates.stream()
                    .filter(rate -> !rate.getEffectiveDate().isBefore(from))
                    .toList();
        }
        if (to != null) {
            rates = rates.stream()
                    .filter(rate -> !rate.getEffectiveDate().isAfter(to))
                    .toList();
        }

        // Filtrera på rateType och term (om angivna)
        if (rateType != null) {
            rates = rates.stream()
                    .filter(rate -> rate.getRateType() == rateType)
                    .toList();
        }
        if (term != null) {
            rates = rates.stream()
                    .filter(rate -> rate.getTerm() == term)
                    .toList();
        }

        // Sortering stigande eller fallande efter datum
        final String sortOrder = (sort == null || sort.isBlank()) ? "asc" : sort;
        final Comparator<MortgageRate> comparator =
                "desc".equalsIgnoreCase(sortOrder)
                        ? Comparator.comparing(MortgageRate::getEffectiveDate).reversed()
                        : Comparator.comparing(MortgageRate::getEffectiveDate);

        // Returnerar filtrerade och sorterade räntor
        return rates.stream().sorted(comparator).toList();
    }

    /**
     * Hämtar alla historiska räntor för flera banker inom valt intervall.
     * Används för trend- och jämförelseanalys.
     */
    public Map<String, List<MortgageRate>> getAllBanksRateHistory(
            List<Bank> banks,
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        return banks.stream()
                .collect(Collectors.toMap(
                        Bank::getName,
                        bank -> getRateHistoryForBank(bank, from, to, sort, null, null)
                ));
    }

    // ===========================================================
    // =============         TREND-BERÄKNINGAR       =============
    // ===========================================================

    /**
     * Beräknar förändringen i bolåneräntor mellan två mättillfällen.
     * - Om from/to anges: jämför exakt dessa datum.
     * - Om from/to saknas och rateType = AVERAGERATE: jämför de två senaste datumen per bank.
     * - Om from/to saknas och rateType = LISTRATE eller null: jämför de två senaste globala datumen.
     */
    public List<RateTrend> getRateTrends(LocalDate from, LocalDate to, String rateType) {
        RateType type = parseRateType(rateType);

        // Om from/to anges – jämför exakt dessa datum
        if (from != null && to != null) {
            List<MortgageRate> latest = mortgageRateRepository.findByEffectiveDate(to);
            List<MortgageRate> prev = mortgageRateRepository.findByEffectiveDate(from);

            if (type != null) {
                latest = filterByType(latest, type);
                prev = filterByType(prev, type);
            }

            return buildTrendsFromTwoSnapshots(prev, latest, from, to);
        }

        // Per bank (snitträntor)
        if (type == RateType.AVERAGERATE) {
            List<RateTrend> out = new ArrayList<>();
            for (Bank bank : bankService.getAllBanks()) {
                List<LocalDate> dates = mortgageRateRepository
                        .findDistinctEffectiveDatesByBankAndRateTypeDesc(bank, RateType.AVERAGERATE);

                if (dates.size() < 2) continue;

                LocalDate latestDate = dates.get(0);
                LocalDate prevDate = dates.get(1);

                List<MortgageRate> latest = mortgageRateRepository
                        .findByBankAndRateTypeAndEffectiveDate(bank, RateType.AVERAGERATE, latestDate);
                List<MortgageRate> prev = mortgageRateRepository
                        .findByBankAndRateTypeAndEffectiveDate(bank, RateType.AVERAGERATE, prevDate);

                out.addAll(buildTrendsFromTwoSnapshots(prev, latest, prevDate, latestDate));
            }

            sortTrends(out);
            return out;
        }

        // Globala senaste datum (listräntor)
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
     * Beräknar alla förändringar i bolåneräntor inom ett valt tidsintervall.
     */
    public List<RateTrend> getRateTrendsInRange(LocalDate from, LocalDate to, String rateType) {
        List<MortgageRate> rates = mortgageRateRepository.findByEffectiveDateBetween(from, to);

        if (rateType != null && !rateType.isBlank()) {
            rates = rates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
        }

        Map<String, List<MortgageRate>> grouped = rates.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getBank().getName() + "_" + r.getTerm() + "_" + r.getRateType()
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
    // =============      HJÄLPMETODER INTERNT      ===============
    // ===========================================================

    private RateType parseRateType(String rateType) {
        if (rateType == null || rateType.isBlank()) return null;
        try {
            return RateType.valueOf(rateType.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Ogiltig rateType: " + rateType);
            return null;
        }
    }

    private List<MortgageRate> filterByType(List<MortgageRate> list, RateType type) {
        return list.stream().filter(r -> r.getRateType() == type).toList();
    }

    /** Matchar bank + term + rateType mellan två datum och bygger RateTrend-lista. */
    private List<RateTrend> buildTrendsFromTwoSnapshots(
            List<MortgageRate> previousRates,
            List<MortgageRate> latestRates,
            LocalDate from,
            LocalDate to
    ) {
        Map<String, Double> prevMap = previousRates.stream().collect(Collectors.toMap(
                r -> r.getBank().getName() + "_" + r.getTerm() + "_" + r.getRateType(),
                r -> r.getRatePercent().doubleValue(),
                (a, b) -> b
        ));

        List<RateTrend> out = new ArrayList<>();
        for (MortgageRate rate : latestRates) {
            String key = rate.getBank().getName() + "_" + rate.getTerm() + "_" + rate.getRateType();
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

    /** Sorterar trender på bank, term, rateType och förändring (fallande). */
    private void sortTrends(List<RateTrend> trends) {
        trends.sort(
                Comparator.comparing(RateTrend::getBankName)
                        .thenComparing(RateTrend::getTerm)
                        .thenComparing(RateTrend::getRateType)
                        .thenComparing(RateTrend::getChange, Comparator.reverseOrder())
        );
    }
}