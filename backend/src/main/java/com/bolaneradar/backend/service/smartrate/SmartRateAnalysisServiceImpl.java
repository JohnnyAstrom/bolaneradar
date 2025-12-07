package com.bolaneradar.backend.service.smartrate;

import com.bolaneradar.backend.dto.api.smartrate.*;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;
import com.bolaneradar.backend.service.smartrate.model.SmartRateAnalysisContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SmartRateAnalysisServiceImpl implements SmartRateAnalysisService {

    private final SmartRateMarketDataService marketService;

    public SmartRateAnalysisServiceImpl(SmartRateMarketDataService marketService) {
        this.marketService = marketService;
    }

    // =========================================================================
    //  PUBLIC ANALYSIS ENTRYPOINT — VERSION 5
    // =========================================================================
    @Override
    public SmartRateTestResult analyze(SmartRateTestRequest request) {

        SmartRateAnalysisContext ctx = buildContext(request);

        if (ctx.hasOffer()) {
            return handleOfferFlow(ctx);
        }

        if (ctx.analyzedTerm() == MortgageTerm.VARIABLE_3M) {
            return handleVariableFlow(ctx);
        }

        return handleFixedFlow(ctx);
    }

    // =========================================================================
//  FLOW B — OFFER FLOW (analyserar ALLA kundens erbjudanden)
// =========================================================================
    // =========================================================================
//  FLOW B — OFFER FLOW (analyserar ALLA kundens erbjudanden)
// =========================================================================
    private SmartRateTestResult handleOfferFlow(SmartRateAnalysisContext ctx) {

        // ----------------------------------------------
        // Fallback — inga erbjudanden angivna
        // ----------------------------------------------
        if (ctx.offers() == null || ctx.offers().isEmpty()) {
            return new SmartRateTestResult(
                    "UNKNOWN",
                    ctx.bankName(),
                    ctx.analyzedTerm(),
                    null,
                    null,
                    "Vi kunde inte läsa in några ränteerbjudanden.",
                    "",
                    "",
                    null,
                    "",
                    List.of(),      // alternatives
                    null,           // alternativesIntro
                    true,           // isOfferFlow
                    List.of(),      // offerAnalyses
                    false           // multipleOffers
            );
        }

        // ----------------------------------------------
        // Analysera samtliga erbjudanden
        // ----------------------------------------------
        List<SmartRateOfferAnalysisResultDto> analyses = new ArrayList<>();

        for (SmartRateOfferDto offer : ctx.offers()) {

            MortgageTerm term = offer.term();
            BigDecimal rate = offer.rate();

            BigDecimal bestMarket = marketService.getMarketBestRate(term);
            BigDecimal medianMarket = marketService.getMarketMedianRate(term);
            BigDecimal bankAvg = marketService.getBankAverageRate(ctx.bankId(), term);

            BigDecimal diffBest = calculateDiff(rate, bestMarket);
            BigDecimal diffMedian = calculateDiff(rate, medianMarket);
            BigDecimal diffBank = calculateDiff(rate, bankAvg);
            BigDecimal yearlyImpact = calculateYearlyImpact(diffBest, ctx.loanAmount());

            String status = classify(diffBest);

            analyses.add(
                    new SmartRateOfferAnalysisResultDto(
                            term,
                            rate,
                            diffBest,
                            diffMedian,
                            diffBank,
                            status,
                            buildAnalysisTextOffer(ctx, rate, diffBest),
                            buildRecommendationForOfferFlow(status, diffBest),
                            yearlyImpact
                    )
            );
        }

        boolean multipleOffers = analyses.size() > 1;

        // ----------------------------------------------
        // Huvudanalysen baseras på det första erbjudandet
        // ----------------------------------------------
        SmartRateOfferAnalysisResultDto primary = analyses.get(0);

        String analysisText = buildPrimaryOfferAnalysisText(
                primary.offeredRate(),
                primary.diffFromBestMarket()
        );

        String recommendation = primary.recommendation();

        // ----------------------------------------------
        // Returnera komplett resultat enligt dina regler
        // ----------------------------------------------
        return new SmartRateTestResult(
                primary.status(),                // status
                ctx.bankName(),                  // bank
                primary.term(),                  // analyzedTerm
                null,                            // differenceFromBankAverage
                null,                            // differenceFromBestMarketAverage
                analysisText,                    // analysisText
                "",                              // additionalContext
                recommendation,                  // recommendation
                null,                            // yearlySaving
                buildPreferenceAdvice(ctx.userPreference(), primary.term()),

                List.of(),                       // alternatives (ALLTID tomt)
                null,                            // alternativesIntro (ALLTID null)

                true,                            // isOfferFlow
                analyses,                        // offerAnalyses
                multipleOffers                   // multipleOffers
        );
    }

    // =========================================================================
    //  FLOW A1 — VARIABLE RATE FLOW
    // =========================================================================
    private SmartRateTestResult handleVariableFlow(SmartRateAnalysisContext ctx) {

        BigDecimal rate = ctx.userRate();

        BigDecimal diffBest = calculateDiff(rate, ctx.marketBestRate());
        BigDecimal diffBank = calculateDiff(rate, ctx.bankLatestAverage());
        BigDecimal diffMedian = calculateDiff(rate, ctx.marketMedianRate());

        String status = classify(diffBest);

        // Ingen potentiell besparing visas i normal analys (endast i offer-flow)
        BigDecimal yearlyImpact = null;

        return new SmartRateTestResult(
                status,
                ctx.bankName(),
                ctx.analyzedTerm(),
                diffBank,
                diffBest,
                buildAnalysisTextVariable(ctx, rate, diffBest),
                buildContextText(diffMedian),
                buildRecommendation(status, diffBest),
                yearlyImpact,
                buildPreferenceAdvice(ctx.userPreference(), ctx.analyzedTerm()),
                generateAlternatives(ctx), // normalt flöde → visa alternativ
                null,                     // alternativesIntro → ej relevant
                false,                    // isOfferFlow → nej
                List.of(),                // offerAnalyses → tom lista
                false                     // multipleOffers → ej offer-flow
        );
    }

    // =========================================================================
    //  FLOW A2 — FIXED RATE FLOW
    // =========================================================================
    private SmartRateTestResult handleFixedFlow(SmartRateAnalysisContext ctx) {

        BigDecimal rate = ctx.userRate();
        BigDecimal diffBank = calculateDiff(rate, ctx.bankLatestAverage());
        BigDecimal diffMedian = calculateDiff(rate, ctx.marketMedianRate());

        Integer months = ctx.monthsUntilExpiration();

        String analysisText;
        String recommendation;

        // Scenario A: Löper ut inom kort (1–3 månader)
        if (months != null && months >= 1 && months <= 3) {
            analysisText = buildAnalysisTextFixedShortTerm(rate);
            recommendation = buildRecommendationFixedShortTerm();
        }

        // Scenario B: Längre tid kvar (>3 månader)
        else if (months != null && months > 3) {
            analysisText = buildAnalysisTextFixedLongTerm(rate);
            recommendation = buildRecommendationFixedLongTerm();
        }

        // Scenario C: Mycket snart eller okänt
        else {
            analysisText = buildAnalysisTextFixedVeryShort(rate);
            recommendation = buildRecommendationFixedVeryShort();
        }

        return new SmartRateTestResult(
                "INFO",
                ctx.bankName(),
                ctx.analyzedTerm(),
                diffBank,
                null,
                analysisText,
                buildContextText(diffMedian),
                recommendation,
                null,                                   // yearlySaving
                buildPreferenceAdvice(ctx.userPreference(), ctx.analyzedTerm()),
                generateAlternatives(ctx),              // normal flow → visa alternativ
                null,                                   // alternativesIntro → ej relevant
                false,                                  // isOfferFlow
                List.of(),                              // offerAnalyses
                false                                   // multipleOffers
        );
    }

    // =========================================================================
    //  ALTERNATIVE LIST — CUSTOMER-FRIENDLY VERSION
    // =========================================================================
    private List<SmartRateAlternative> generateAlternatives(SmartRateAnalysisContext ctx) {

        // Hämta bästa erbjudandet om offer-flow
        SmartRateOfferDto bestOffer = ctx.hasOffer()
                ? findBestOffer(ctx.offers())
                : null;

        List<MortgageTerm> terms;

        // --------------------------------------------------------------
        // 1. OFFERT-FLOW → visa alternativ baserat på bästa erbjudandet
        // --------------------------------------------------------------
        if (bestOffer != null) {
            terms = List.of(bestOffer.term());
        }

        // --------------------------------------------------------------
        // 2. NORMALT FLOW → basera på kundens preferens
        // --------------------------------------------------------------
        else if (ctx.userPreference() != null) {
            terms = marketService.getTermsForPreference(ctx.userPreference());
        }

        // --------------------------------------------------------------
        // 3. Saknar både preferens och erbjudande → inga alternativ
        // --------------------------------------------------------------
        else {
            return List.of();
        }

        List<SmartRateAlternative> list = new ArrayList<>();

        // User rate = erbjuden ränta eller kundens egna ränta
        BigDecimal userRate = bestOffer != null
                ? bestOffer.rate()
                : ctx.userRate();

        for (MortgageTerm t : terms) {

            // Hämta marknadens median-snitt
            BigDecimal avg = marketService.getMarketMedianRate(t);
            if (avg == null) continue;

            BigDecimal diff = calculateDiff(avg, userRate);
            BigDecimal yearlyImpact = calculateYearlyImpact(diff, ctx.loanAmount());

            list.add(new SmartRateAlternative(
                    t,
                    avg,
                    diff,
                    yearlyImpact
            ));
        }

        return list;
    }

    // ============================================================================
    //  TEXTBUILDERS — OFFER FLOW (B-FLOW)
    //  Används ENDAST när kunden har ett eller flera ränteerbjudanden
    // ============================================================================

    /** Full analys av varje enskilt erbjudande */
    private String buildAnalysisTextOffer(SmartRateAnalysisContext ctx, BigDecimal rate, BigDecimal diffBest) {
        return "Vi har analyserat ditt ränteerbjudande på " + rate + "%. "
                + "Erbjudandet ligger " + formatDiff(diffBest) + " jämfört med den lägsta aktuella snitträntan på marknaden. "
                + "Det betyder att ditt erbjudande står sig " + (diffBest.compareTo(BigDecimal.ZERO) > 0 ? "sämre" : "bättre")
                + " än vad många andra kunder får just nu.";
    }

    /** Huvudanalysen i offer-flow, baserad på det FÖRSTA erbjudandet */
    private String buildPrimaryOfferAnalysisText(BigDecimal offeredRate, BigDecimal diffBestMarket) {

        String betterOrWorse = (diffBestMarket.compareTo(BigDecimal.ZERO) > 0)
                ? "sämre"
                : "bättre";

        return "Vi har analyserat ditt ränteerbjudande på "
                + formatRate(offeredRate) + "%. "
                + "Erbjudandet ligger " + formatDiff(diffBestMarket)
                + " jämfört med den lägsta aktuella snitträntan på marknaden. "
                + "Det betyder att ditt erbjudande står sig " + betterOrWorse
                + " än vad många andra kunder får just nu.";
    }

    /** Anpassad rekommendationstext för erbjudanden */
    private String buildRecommendationForOfferFlow(String status, BigDecimal diff) {
        String base = buildRecommendation(status, diff);

        return base
                .replace("Din ränta", "Ditt erbjudande")
                .replace("din ränta", "ditt erbjudande")
                .replace("Du har en bra nivå", "Erbjudandet ligger på en bra nivå")
                .replace("du har", "erbjudandet har");
    }

    /** Kontexttext specifikt för erbjudanden */
    private String buildOfferContextText(BigDecimal diffMedian) {

        if (diffMedian == null) {
            return "Vi jämför ditt erbjudande med bankernas publicerade snitträntor för att ge en rättvis bild av marknadsläget.";
        }

        return "Vi jämför ditt erbjudande med bankernas publicerade snitträntor. "
                + "Ditt erbjudande ligger " + formatDiff(diffMedian) + " jämfört med genomsnittet.";
    }

    /** Introtext för alternativlistan i offer-flow (används ej just nu men sparas) */
    private String buildOfferAlternativesIntro() {
        return "Här ser du hur ditt erbjudande står sig mot marknadens räntor. "
                + "Vi jämför med aktuella nivåer för bindningstider som passar dina preferenser, "
                + "så att du enkelt kan se om det finns mer fördelaktiga alternativ.";
    }

    // ============================================================================
    //  TEXTBUILDERS — VARIABLE RATE FLOW (A1-FLOW)
    //  Används när kunden har rörlig ränta och INTE har offer-flow
    // ============================================================================

    private String buildAnalysisTextVariable(SmartRateAnalysisContext ctx, BigDecimal rate, BigDecimal diffBest) {
        return "Du har en rörlig ränta på " + rate + "%. "
                + "Rörlig ränta innebär att du kan förhandla eller byta bank när som helst, eftersom du inte är bunden vid någon löptid. "
                + "Din nuvarande nivå ligger " + formatDiff(diffBest)
                + " jämfört med den lägsta aktuella snitträntan på marknaden.";
    }

    private String buildContextText(BigDecimal diffMedian) {

        if (diffMedian == null)
            return "Jämförelsen baseras på bankernas publicerade snitträntor.";

        return "Jämförelsen baseras på bankernas publicerade snitträntor. "
                + "Din nivå ligger " + formatDiff(diffMedian)
                + " jämfört med genomsnittet av dessa.";
    }

    /** Generell rekommendation för rörlig + fast ränta när ingen erbjudande finns */
    private String buildRecommendation(String status, BigDecimal diff) {

        if (diff == null) {
            return "Vi saknar viss marknadsdata och kan därför inte ge en fullständig rekommendation.";
        }

        if (status.equals("GREAT_GREEN")) {
            return "Din ränta ligger betydligt bättre till än vad många andra kunder betalar idag. "
                    + "Du har en mycket bra nivå. Fortsätt gärna hålla ett öga på marknaden ibland, men du behöver normalt inte göra något just nu.";
        }

        if (status.equals("GREEN")) {
            return "Din ränta ligger i linje med marknaden. "
                    + "Det är en bra nivå, men det kan ändå vara klokt att ibland stämma av med banken för att säkerställa att du får deras bästa erbjudande.";
        }

        if (status.equals("YELLOW")) {
            return "Din ränta ligger något högre än marknadens nivåer. "
                    + "Det kan vara ett bra tillfälle att kontakta banken och höra om de kan förbättra din ränta eller matcha bättre erbjudanden.";
        }

        if (status.equals("ORANGE")) {
            return "Din ränta är tydligt högre än vad många andra kunder erbjuds idag. "
                    + "Det kan löna sig att förhandla, eller att jämföra med andra banker för att se om du kan få en lägre nivå.";
        }

        if (status.equals("RED")) {
            return "Din ränta ligger betydligt högre än marknadens nivåer. "
                    + "Du har sannolikt mycket att vinna på att förhandla, eller att jämföra erbjudanden från flera banker för att hitta en bättre nivå.";
        }

        return "Vi saknar viss marknadsdata och kan därför inte ge en fullständig rekommendation.";
    }


    // ============================================================================
    //  TEXTBUILDERS — FIXED RATE FLOW (A2-FLOW)
    //  Används när kunden har bunden ränta och INTE har offer-flow
    // ============================================================================

    private String buildAnalysisTextFixedShortTerm(BigDecimal rate) {
        return
                "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer."
                        + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                        + "Du har en bunden ränta på " + rate + "%. Din bindningstid löper ut inom kort, "
                        + "vilket innebär att du snart kan välja ny ränta utan kostnad."
                        + "Det är vanligt att man förhandlar räntan när det är mindre än en månad kvar av bindningstiden, "
                        + "men det kan vara smart att börja förbereda sig redan nu.\n\n"
                        + "Här visar vi marknadsläget just nu för att hjälpa dig inför ditt kommande bindningsval och ge en tydlig bild "
                        + "av vilka nivåer som är konkurrenskraftiga idag.";
    }

    private String buildRecommendationFixedShortTerm() {
        return "Eftersom din bindningstid snart löper ut är det ett bra läge att börja titta på olika alternativ "
                + "och fundera på vilken bindningstid som passar dig bäst framöver.";
    }

    private String buildAnalysisTextFixedLongTerm(BigDecimal rate) {
        return
                "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer."
                        + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                        + "Du har en bunden ränta på " + rate + "%. Det är lång tid kvar tills bindningstiden löper ut, "
                        + "vilket innebär att du normalt inte kan omförhandla eller byta ränta kostnadsfritt ännu."
                        + "Om du vill undersöka möjligheten att byta i förtid kan du be din bank om en uppgift på eventuell "
                        + "ränteskillnadsersättning.\n\n"
                        + "Vi visar ändå hur ränteläget ser ut just nu, så att du redan nu får en bild av marknaden inför ditt nästa bindningsval.";
    }


    private String buildRecommendationFixedLongTerm() {
        return "Ett bra tillfälle att göra en ny räntekoll är när det är mindre än en månad kvar av bindningstiden. "
                + "Du kan redan nu undersöka om ränteskillnadsersättningen är låg, men de flesta får bäst möjligheter "
                + "att förhandla när bindningstiden närmar sig sitt slut.";
    }

    private String buildAnalysisTextFixedVeryShort(BigDecimal rate) {
        return
                "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer."
                        + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                        + "Du har en bunden ränta på " + rate + "%. Din bindningstid löper ut mycket snart, "
                        + "vilket innebär att du nu befinner dig i ett optimalt läge att förhandla om en ny ränta.\n\n"
                        + "Här visar vi hur marknaden ser ut just nu för att hjälpa dig inför ditt kommande bindningsval.";
    }

    private String buildRecommendationFixedVeryShort() {
        return "När det är mindre än en månad kvar av bindningstiden är det vanligt att påbörja ränteförhandling. "
                + "Kontakta gärna banken för att höra vilka nivåer de kan erbjuda.";
    }


    // ============================================================================
    //  TEXTBUILDERS — PREFERENCE TEXTS
    //  Används för alla flöden om kund anger preferens
    // ============================================================================

    private String buildPreferenceAdvice(RatePreference pref, MortgageTerm analyzedTerm) {

        if (pref == null) return "";

        return switch (pref) {

            case VARIABLE_3M ->
                    "Rörlig ränta passar dig som prioriterar flexibilitet och är okej med att kostnaden kan variera över tid. "
                            + "Den kan både stiga och sjunka, men ger dig frihet att byta bank eller binda när läget känns rätt.";

            case SHORT ->
                    "Korta bindningstider (1–3 år) passar dig som vill ha en viss trygghet men ändå behålla flexibilitet på några års sikt. "
                            + "Ett bra val om du vill säkra kostnaden en period utan att låsa dig för lång tid.";

            case LONG ->
                    "Längre bindningstider (4–10 år) passar dig som vill ha hög förutsägbarhet och skydda dig mot framtida räntehöjningar. "
                            + "Tänk på att flexibiliteten minskar och att ränteskillnadsersättning kan tillkomma om du löser lånet i förtid.";
        };
    }


    // ============================================================================
    //  TEXTBUILDERS — SHARED HELPERS
    //  Används i flera olika flows
    // ============================================================================

    /** Förenklad formattering av skillnadstext */
    private String formatDiff(BigDecimal diff) {
        if (diff == null) return "okänt";

        BigDecimal abs = diff.abs();
        int cmp = diff.compareTo(BigDecimal.ZERO);

        if (cmp > 0) return abs + "% högre";
        if (cmp < 0) return abs + "% lägre";
        return "i nivå med";
    }

    /** Formaterar ränta med exakt två decimaler, används i offer-flow */
    private String formatRate(BigDecimal rate) {
        if (rate == null) return "okänd nivå";
        return rate.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }


    // =========================================================================
    //  HELPERS
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

    private String classify(BigDecimal diff) {

        if (diff == null) return "UNKNOWN";

        BigDecimal strongBetter = new BigDecimal("-0.30");   // mycket bättre än marknaden
        BigDecimal slightlyHigher = new BigDecimal("0.30");  // något högre
        BigDecimal noticeablyHigher = new BigDecimal("0.70"); // markant högre

        // MYCKET bättre än marknaden (t ex –0.40%, –0.60%)
        if (diff.compareTo(strongBetter) <= 0) {
            return "GREAT_GREEN";
        }

        // I nivå eller lite bättre (–0.29% till 0%)
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return "GREEN";
        }

        // Lite högre (0% till +0.30%)
        if (diff.compareTo(slightlyHigher) <= 0) {
            return "YELLOW";
        }

        // Markant högre (+0.30% till +0.70%)
        if (diff.compareTo(noticeablyHigher) <= 0) {
            return "ORANGE";
        }

        // Extremt mycket högre (> +0.70%)
        return "RED";
    }

    private Integer calculateMonthsUntilExpiration(LocalDate endDate) {
        if (endDate == null) return null;

        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, endDate);

        if (days < 0) return 0; // redan passerat

        // definiera "inom 3 månader" som ≤ 90 dagar
        if (days <= 90) {
            return (int) (days / 30);
        }

        // Om det är längre än 90 dagar ska vi *inte* hamna i "inom kort".
        // Därför returnerar vi en siffra > 3.
        long months = ChronoUnit.MONTHS.between(today.withDayOfMonth(1), endDate.withDayOfMonth(1));

        // Men om det är 3m + X dagar, ska months vara MINST 4
        if (months <= 3) {
            months = 4;
        }

        return (int) months;
    }

    private SmartRateOfferDto findBestOffer(List<SmartRateOfferDto> offers) {
        if (offers == null || offers.isEmpty()) return null;

        return offers.stream()
                .min(Comparator.comparing(SmartRateOfferDto::rate))
                .orElse(null);
    }

    // =========================================================================
    //  CONTEXT BUILDER
    // =========================================================================
    private SmartRateAnalysisContext buildContext(SmartRateTestRequest request) {

        MortgageTerm analyzedTerm;

        if (request.hasOffer()
                && request.offers() != null
                && !request.offers().isEmpty()) {
            analyzedTerm = request.offers().get(0).term(); // default: första erbjudandet
        } else {
            analyzedTerm = request.userCurrentTerm() != null
                    ? request.userCurrentTerm()
                    : MortgageTerm.VARIABLE_3M;
        }

        // Beräkning av månader tills bindningstiden löper ut
        Integer monthsUntilExpiration = null;
        if (request.bindingEndDate() != null) {
            monthsUntilExpiration = calculateMonthsUntilExpiration(request.bindingEndDate());
        }

        return new SmartRateAnalysisContext(
                request.hasOffer(),
                request.bankId(),
                request.bankName(),
                request.userRate(),
                request.userCurrentTerm(),
                request.offers(),
                request.userPreference(),
                marketService.getBankAverageRate(request.bankId(), analyzedTerm),
                marketService.getMarketBestRate(analyzedTerm),
                marketService.getMarketMedianRate(analyzedTerm),
                null,
                analyzedTerm,
                request.loanAmount(),
                monthsUntilExpiration
        );
    }
}