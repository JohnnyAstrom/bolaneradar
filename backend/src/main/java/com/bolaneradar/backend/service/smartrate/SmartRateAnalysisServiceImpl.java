package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.*;
import com.bolaneradar.backend.entity.enums.Language;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RateComparison;
import com.bolaneradar.backend.entity.enums.smartrate.SmartRateStatus;
import com.bolaneradar.backend.service.smartrate.model.MarketSnapshot;
import com.bolaneradar.backend.service.smartrate.model.SmartRateAnalysisContext;
import com.bolaneradar.backend.service.smartrate.text.SmartRateTexts;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class SmartRateAnalysisServiceImpl implements SmartRateAnalysisService {

    private static final Logger log =
            LoggerFactory.getLogger(SmartRateAnalysisServiceImpl.class);

    private final SmartRateMarketDataService marketService;

    public SmartRateAnalysisServiceImpl(SmartRateMarketDataService marketService) {
        this.marketService = marketService;
    }

    // =========================================================================
    // PUBLIC ENTRYPOINT
    // =========================================================================
    @Override
    public SmartRateTestResult analyze(SmartRateTestRequest request) {

        long t0 = System.currentTimeMillis();

        long tCtx0 = System.currentTimeMillis();
        SmartRateAnalysisContext ctx = buildContext(request);
        long tCtx1 = System.currentTimeMillis();

        SmartRateTestResult result;

        // ===== STEG 3: bygg ALLA termer som behövs =====
        Set<MortgageTerm> terms = new java.util.HashSet<>();

        // alltid med aktuell term
        terms.add(ctx.analyzedTerm());

        // offer-termer (om finns)
        if (ctx.hasOffer()) {
            ctx.offers().stream()
                    .map(SmartRateOfferDto::term)
                    .forEach(terms::add);
        }

        // preferens-termer (för alternativ-tabellen)
        if (ctx.userPreference() != null) {
            terms.addAll(marketService.getTermsForPreference(ctx.userPreference()));
        }

        // ===== snapshot byggs EN gång =====
        MarketSnapshot snapshot =
                marketService.getMarketSnapshot(ctx.bankId(), terms);

        // ===== Kör rätt flow =====
        if (ctx.hasOffer()) {
            long tFlow0 = System.currentTimeMillis();
            result = handleOfferFlow(ctx, snapshot);
            long tFlow1 = System.currentTimeMillis();

            log.info("[SmartRate] offerFlow ms={}", (tFlow1 - tFlow0));

        } else if (ctx.analyzedTerm() == MortgageTerm.VARIABLE_3M) {
            long tFlow0 = System.currentTimeMillis();
            result = handleVariableFlow(ctx, snapshot);
            long tFlow1 = System.currentTimeMillis();

            log.info("[SmartRate] variableFlow ms={}", (tFlow1 - tFlow0));

        } else {
            long tFlow0 = System.currentTimeMillis();
            result = handleFixedFlow(ctx, snapshot);
            long tFlow1 = System.currentTimeMillis();

            log.info("[SmartRate] fixedFlow ms={}", (tFlow1 - tFlow0));
        }

        long t1 = System.currentTimeMillis();

        log.info(
                "[SmartRate] buildContext ms={}, total ms={}",
                (tCtx1 - tCtx0),
                (t1 - t0)
        );

        return result;
    }

    // =========================================================================
    // FLOW B — OFFER FLOW
    // =========================================================================
    private SmartRateTestResult handleOfferFlow(SmartRateAnalysisContext ctx, MarketSnapshot snapshot) {

        SmartRateTexts texts = SmartRateTexts.of(ctx.language());

        if (ctx.offers() == null || ctx.offers().isEmpty()) {
            return new SmartRateTestResult(
                    SmartRateStatus.UNKNOWN.name(),
                    ctx.bankName(),
                    ctx.analyzedTerm(),
                    null,
                    null,
                    texts.offerAnalysis(null, null, RateComparison.UNKNOWN),
                    "",
                    "",
                    null,
                    "",
                    List.of(),
                    null,
                    true,
                    List.of(),
                    false
            );
        }

        List<SmartRateOfferAnalysisResultDto> analyses = new ArrayList<>();

        for (SmartRateOfferDto offer : ctx.offers()) {

            MortgageTerm term = offer.term();
            BigDecimal rate = offer.rate();

            BigDecimal bestMarket = snapshot.bestByTerm().get(term);
            BigDecimal medianMarket = snapshot.medianByTerm().get(term);
            BigDecimal bankAvg = snapshot.bankAvgByTerm().get(term);

            BigDecimal diffBest = calculateDiff(rate, bestMarket);
            BigDecimal diffMedian = calculateDiff(rate, medianMarket);
            BigDecimal diffBank = calculateDiff(rate, bankAvg);
            BigDecimal yearlyImpact = calculateYearlyImpact(diffBest, ctx.loanAmount());

            SmartRateStatus status = classify(diffBest);
            RateComparison bestCmp = classifyDiff(diffBest);

            analyses.add(
                    new SmartRateOfferAnalysisResultDto(
                            term,
                            rate,
                            diffBest,
                            diffMedian,
                            diffBank,
                            status.name(),
                            texts.offerAnalysis(
                                    rate,
                                    absDiff(diffBest),
                                    bestCmp
                            ),
                            texts.recommendation(status),
                            yearlyImpact
                    )
            );
        }

        SmartRateOfferAnalysisResultDto primary = analyses.get(0);
        boolean multipleOffers = analyses.size() > 1;

        RateComparison primaryCmp =
                classifyDiff(primary.diffFromBestMarket());

        return new SmartRateTestResult(
                primary.status(),
                ctx.bankName(),
                primary.term(),
                null,
                null,
                texts.offerAnalysis(
                        primary.offeredRate(),
                        absDiff(primary.diffFromBestMarket()),
                        primaryCmp
                ),
                "",
                primary.recommendation(),
                null,
                texts.preferenceAdvice(ctx.userPreference()),
                List.of(),
                null,
                true,
                analyses,
                multipleOffers
        );
    }

    // =========================================================================
    // FLOW A1 — VARIABLE RATE
    // =========================================================================
    private SmartRateTestResult handleVariableFlow(
            SmartRateAnalysisContext ctx,
            MarketSnapshot snapshot
    ) {

        MortgageTerm term = ctx.analyzedTerm();
        BigDecimal rate = ctx.userRate();

        BigDecimal best = snapshot.bestByTerm().get(term);
        BigDecimal median = snapshot.medianByTerm().get(term);
        BigDecimal bankAvg = snapshot.bankAvgByTerm().get(term);

        BigDecimal diffBest = calculateDiff(rate, best);
        BigDecimal diffBank = calculateDiff(rate, bankAvg);
        BigDecimal diffMedian = calculateDiff(rate, median);

        SmartRateStatus status = classify(diffBest);

        RateComparison bestCmp = classifyDiff(diffBest);
        RateComparison medianCmp = classifyDiff(diffMedian);

        SmartRateTexts texts = SmartRateTexts.of(ctx.language());

        return new SmartRateTestResult(
                status.name(),
                ctx.bankName(),
                term,
                diffBank,
                diffBest,
                texts.variableAnalysis(
                        rate,
                        absDiff(diffBest),
                        bestCmp
                ),
                texts.variableContext(
                        absDiff(diffMedian),
                        medianCmp
                ),
                texts.recommendation(status),
                null,
                texts.preferenceAdvice(ctx.userPreference()),
                generateAlternatives(ctx, snapshot),
                null,
                false,
                List.of(),
                false
        );
    }


    // =========================================================================
    // FLOW A2 — FIXED RATE
    // =========================================================================
    private SmartRateTestResult handleFixedFlow(
            SmartRateAnalysisContext ctx,
            MarketSnapshot snapshot
    ) {

        MortgageTerm term = ctx.analyzedTerm();
        BigDecimal rate = ctx.userRate();

        BigDecimal median = snapshot.medianByTerm().get(term);
        BigDecimal bankAvg = snapshot.bankAvgByTerm().get(term);

        BigDecimal diffBank = calculateDiff(rate, bankAvg);
        BigDecimal diffMedian = calculateDiff(rate, median);

        RateComparison medianCmp = classifyDiff(diffMedian);

        Integer months = ctx.monthsUntilExpiration();
        SmartRateTexts texts = SmartRateTexts.of(ctx.language());

        String analysisText;
        String recommendation;

        if (months != null && months >= 1 && months <= 3) {
            analysisText = texts.fixedAnalysisShortTerm(rate);
            recommendation = texts.fixedRecommendationShortTerm();
        } else if (months != null && months > 3) {
            analysisText = texts.fixedAnalysisLongTerm(rate);
            recommendation = texts.fixedRecommendationLongTerm();
        } else {
            analysisText = texts.fixedAnalysisVeryShort(rate);
            recommendation = texts.fixedRecommendationVeryShort();
        }

        return new SmartRateTestResult(
                SmartRateStatus.INFO.name(),
                ctx.bankName(),
                term,
                diffBank,
                null,
                analysisText,
                texts.variableContext(
                        absDiff(diffMedian),
                        medianCmp
                ),
                recommendation,
                null,
                texts.preferenceAdvice(ctx.userPreference()),
                generateAlternatives(ctx, snapshot),
                null,
                false,
                List.of(),
                false
        );
    }

    // =========================================================================
    // ALTERNATIVES
    // =========================================================================
    private List<SmartRateAlternative> generateAlternatives(SmartRateAnalysisContext ctx, MarketSnapshot snapshot) {

        SmartRateOfferDto bestOffer = ctx.hasOffer()
                ? findBestOffer(ctx.offers())
                : null;

        List<MortgageTerm> terms;

        if (bestOffer != null) {
            terms = List.of(bestOffer.term());
        } else if (ctx.userPreference() != null) {
            terms = marketService.getTermsForPreference(ctx.userPreference());
        } else {
            return List.of();
        }

        List<SmartRateAlternative> list = new ArrayList<>();

        BigDecimal userRate = bestOffer != null
                ? bestOffer.rate()
                : ctx.userRate();

        for (MortgageTerm term : terms) {

            BigDecimal avg = snapshot.medianByTerm().get(term);
            if (avg == null) continue;

            BigDecimal diff = calculateDiff(avg, userRate);
            BigDecimal yearlyImpact = calculateYearlyImpact(diff, ctx.loanAmount());

            list.add(new SmartRateAlternative(
                    term,
                    avg,
                    diff,
                    yearlyImpact
            ));
        }

        return list;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private BigDecimal calculateDiff(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return null;
        return a.subtract(b);
    }

    private BigDecimal calculateYearlyImpact(BigDecimal diff, BigDecimal loanAmount) {
        if (diff == null || loanAmount == null) return null;

        return loanAmount.multiply(diff)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private SmartRateStatus classify(BigDecimal diff) {

        if (diff == null) return SmartRateStatus.UNKNOWN;

        if (diff.compareTo(new BigDecimal("-0.30")) <= 0) return SmartRateStatus.GREAT_GREEN;
        if (diff.compareTo(BigDecimal.ZERO) <= 0) return SmartRateStatus.GREEN;
        if (diff.compareTo(new BigDecimal("0.30")) <= 0) return SmartRateStatus.YELLOW;
        if (diff.compareTo(new BigDecimal("0.70")) <= 0) return SmartRateStatus.ORANGE;

        return SmartRateStatus.RED;
    }

    private RateComparison classifyDiff(BigDecimal diff) {
        if (diff == null) return RateComparison.UNKNOWN;

        int cmp = diff.compareTo(BigDecimal.ZERO);
        if (cmp > 0) return RateComparison.HIGHER;
        if (cmp < 0) return RateComparison.LOWER;
        return RateComparison.EQUAL;
    }

    private BigDecimal absDiff(BigDecimal diff) {
        return diff == null
                ? null
                : diff.abs().setScale(2, RoundingMode.HALF_UP);
    }


    private SmartRateOfferDto findBestOffer(List<SmartRateOfferDto> offers) {
        if (offers == null || offers.isEmpty()) return null;

        return offers.stream()
                .min(Comparator.comparing(SmartRateOfferDto::rate))
                .orElse(null);
    }

    // =========================================================================
    // CONTEXT BUILDER
    // =========================================================================
    private SmartRateAnalysisContext buildContext(SmartRateTestRequest request) {

        MortgageTerm analyzedTerm;

        if (request.hasOffer() && request.offers() != null && !request.offers().isEmpty()) {
            analyzedTerm = request.offers().get(0).term();
        } else {
            analyzedTerm = request.userCurrentTerm() != null
                    ? request.userCurrentTerm()
                    : MortgageTerm.VARIABLE_3M;
        }

        Integer monthsUntilExpiration = null;
        if (request.bindingEndDate() != null) {
            monthsUntilExpiration = calculateMonthsUntilExpiration(request.bindingEndDate());
        }

        Language lang = request.language() != null
                ? request.language()
                : Language.SV;

        return new SmartRateAnalysisContext(
                request.hasOffer(),
                request.bankId(),
                request.bankName(),
                request.userRate(),
                request.userCurrentTerm(),
                request.offers(),
                request.userPreference(),
                null,   // bankAvg tas från snapshot
                null,   // marketBest tas från snapshot
                null,   // marketMedian tas från snapshot
                null,
                analyzedTerm,
                request.loanAmount(),
                monthsUntilExpiration,
                lang
        );
    }

    private Integer calculateMonthsUntilExpiration(LocalDate endDate) {
        if (endDate == null) return null;

        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (days < 0) return 0;

        if (days <= 90) return (int) (days / 30);

        long months = ChronoUnit.MONTHS.between(
                LocalDate.now().withDayOfMonth(1),
                endDate.withDayOfMonth(1)
        );

        return months <= 3 ? 4 : (int) months;
    }
}