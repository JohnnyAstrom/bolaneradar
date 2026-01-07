package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.smartrate.model.MarketSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SmartRateMarketDataServiceImpl implements SmartRateMarketDataService {

    private final MortgageRateRepository repo;

    public SmartRateMarketDataServiceImpl(MortgageRateRepository repo) {
        this.repo = repo;
    }

    // =========================================================================
    // 1. Bankens senaste snittränta
    // =========================================================================
    @Override
    public BigDecimal getBankAverageRate(Long bankId, MortgageTerm term) {

        MortgageRate rate = repo.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                bankId,
                term,
                RateType.AVERAGERATE
        );

        return rate != null ? rate.getRatePercent() : null;
    }

    // =========================================================================
    // 2. Marknadens bästa snittränta
    // =========================================================================
    @Override
    public BigDecimal getMarketBestRate(MortgageTerm term) {

        List<MortgageRate> latest = repo.findLatestRatesByType(RateType.AVERAGERATE);

        return latest.stream()
                .filter(m -> m.getTerm() == term)
                .map(MortgageRate::getRatePercent)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    // =========================================================================
    // 3. Marknadens median snittränta
    // =========================================================================
    @Override
    public BigDecimal getMarketMedianRate(MortgageTerm term) {

        List<BigDecimal> values = repo.findLatestRatesByType(RateType.AVERAGERATE)
                .stream()
                .filter(m -> m.getTerm() == term)
                .map(MortgageRate::getRatePercent)
                .sorted()
                .toList();

        if (values.isEmpty()) return null;

        int size = values.size();
        int mid = size / 2;

        return (size % 2 == 1)
                ? values.get(mid)
                : values.get(mid - 1).add(values.get(mid)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    // =========================================================================
    // 4. Historisk rörlig snittränta vid ränteändringsdag
    // =========================================================================
    @Override
    public BigDecimal getHistoricVariableRate(Long bankId, LocalDate date) {

        YearMonth ym = YearMonth.from(date);

        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.plusMonths(1).atDay(1);

        MortgageRate rate = repo.findAverageRateForBankAndTermAndMonth(
                bankId,
                MortgageTerm.VARIABLE_3M,
                monthStart,
                monthEnd
        );

        return rate != null ? rate.getRatePercent() : null;
    }

    // =========================================================================
    // 5. Terms för preferens
    // =========================================================================
    @Override
    public List<MortgageTerm> getTermsForPreference(RatePreference pref) {

        return switch (pref) {

            case VARIABLE_3M ->
                    List.of(MortgageTerm.VARIABLE_3M);

            case SHORT ->
                    List.of(
                            MortgageTerm.FIXED_1Y,
                            MortgageTerm.FIXED_2Y,
                            MortgageTerm.FIXED_3Y
                    );

            case LONG ->
                    List.of(
                            MortgageTerm.FIXED_4Y,
                            MortgageTerm.FIXED_5Y,
                            MortgageTerm.FIXED_7Y,
                            MortgageTerm.FIXED_10Y
                    );
        };
    }

    // =========================================================================
    // 6. SNAPSHOT – ENDA NYA DELEN
    // =========================================================================
    @Override
    public MarketSnapshot getMarketSnapshot(
            Long bankId,
            Set<MortgageTerm> terms
    ) {

        // ===== EN DB-QUERY =====
        List<MortgageRate> latest =
                repo.findLatestRatesByType(RateType.AVERAGERATE);

        // =========================================================================
        // Bankens snitträntor per term
        // =========================================================================
        var bankAvgByTerm = latest.stream()
                .filter(r -> r.getBank() != null)
                .filter(r -> r.getBank().getId().equals(bankId))
                .filter(r -> terms.contains(r.getTerm()))
                .collect(Collectors.toMap(
                        MortgageRate::getTerm,
                        MortgageRate::getRatePercent,
                        (a, b) -> a
                ));

        // =========================================================================
        // Marknadens bästa ränta per term
        // =========================================================================
        var bestByTerm = latest.stream()
                .filter(r -> terms.contains(r.getTerm()))
                .collect(Collectors.groupingBy(
                        MortgageRate::getTerm,
                        Collectors.mapping(
                                MortgageRate::getRatePercent,
                                Collectors.minBy(Comparator.naturalOrder())
                        )
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));

        // =========================================================================
        // Marknadens medianränta per term
        // =========================================================================
        var medianByTerm = latest.stream()
                .filter(r -> terms.contains(r.getTerm()))
                .collect(Collectors.groupingBy(
                        MortgageRate::getTerm,
                        Collectors.mapping(
                                MortgageRate::getRatePercent,
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                // filtrera bort tomma listor
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<BigDecimal> values = e.getValue().stream()
                                    .sorted()
                                    .toList();

                            int size = values.size();
                            int mid = size / 2;

                            return (size % 2 == 1)
                                    ? values.get(mid)
                                    : values.get(mid - 1)
                                    .add(values.get(mid))
                                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                        }
                ));

        return new MarketSnapshot(
                Map.copyOf(bestByTerm),
                Map.copyOf(medianByTerm),
                Map.copyOf(bankAvgByTerm)
        );
    }
}
