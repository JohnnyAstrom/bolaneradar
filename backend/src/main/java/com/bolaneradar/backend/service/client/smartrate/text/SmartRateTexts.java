package com.bolaneradar.backend.service.client.smartrate.text;

import com.bolaneradar.backend.entity.enums.Language;
import com.bolaneradar.backend.entity.enums.smartrate.RateComparison;
import com.bolaneradar.backend.entity.enums.smartrate.RatePreference;
import com.bolaneradar.backend.entity.enums.smartrate.SmartRateStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SmartRateTexts {

    private final Language lang;

    private SmartRateTexts(Language lang) {
        this.lang = lang;
    }

    public static SmartRateTexts of(Language lang) {
        return new SmartRateTexts(lang);
    }

    // =========================
    // VARIABLE RATE
    // =========================

    public String variableAnalysis(BigDecimal rate, BigDecimal diff, RateComparison comparison)
    {
        String diffText = diffLabel(diff, comparison);

        return switch (lang) {

            case SV ->
                    "Du har en rörlig ränta på " + rate + "%. "
                            + "Rörlig ränta innebär att du kan förhandla eller byta bank när som helst, eftersom du inte är bunden vid någon löptid. "
                            + "Din nuvarande nivå ligger " + diffText
                            + " jämfört med den lägsta aktuella snitträntan på marknaden.";

            case EN ->
                    "You have a variable interest rate of " + rate + "%. "
                            + "A variable interest rate means you can negotiate or switch banks at any time, since you are not bound to a fixed term. "
                            + "Your current rate level is " + diffText
                            + " compared to the lowest average interest rate currently available on the market.";
        };
    }

    // =========================
    // VARIABLE RATE – CONTEXT
    // =========================

    public String variableContext(BigDecimal diff, RateComparison comparison) {

        String diffText = diffLabel(diff, comparison);

        return switch (lang) {

            case SV ->
                    "Jämförelsen baseras på bankernas publicerade snitträntor. "
                            + "Din nivå ligger " + diffText
                            + " jämfört med genomsnittet av dessa.";

            case EN ->
                    "The comparison is based on banks' published average interest rates. "
                            + "Your rate level is " + diffText
                            + " compared to the average of these.";
        };
    }


    // =========================
    // RECOMMENDATION
    // =========================

    public String recommendation(SmartRateStatus status) {
        return switch (lang) {

            case SV -> switch (status) {

                case GREAT_GREEN ->
                        "Din ränta ligger betydligt bättre till än vad många andra kunder betalar idag. "
                                + "Du har en mycket bra nivå. Fortsätt gärna hålla ett öga på marknaden ibland, "
                                + "men du behöver normalt inte göra något just nu.";

                case GREEN ->
                        "Din ränta ligger i linje med marknaden. "
                                + "Det är en bra nivå, men det kan ändå vara klokt att ibland stämma av med banken "
                                + "för att säkerställa att du får deras bästa erbjudande.";

                case YELLOW ->
                        "Din ränta ligger något högre än marknadens nivåer. "
                                + "Det kan vara ett bra tillfälle att kontakta banken och höra om de kan förbättra din ränta "
                                + "eller matcha bättre erbjudanden.";

                case ORANGE ->
                        "Din ränta är tydligt högre än vad många andra kunder erbjuds idag. "
                                + "Det kan löna sig att förhandla, eller att jämföra med andra banker för att se "
                                + "om du kan få en lägre nivå.";

                case RED ->
                        "Din ränta ligger betydligt högre än marknadens nivåer. "
                                + "Du har sannolikt mycket att vinna på att förhandla, eller att jämföra erbjudanden "
                                + "från flera banker för att hitta en bättre nivå.";

                default ->
                        "Vi saknar viss marknadsdata och kan därför inte ge en fullständig rekommendation.";
            };

            case EN -> switch (status) {

                case GREAT_GREEN ->
                        "Your interest rate is significantly better than what many other customers are paying today. "
                                + "You have a very strong rate. It is usually not necessary to take any action right now, "
                                + "but it can still be wise to keep an eye on the market from time to time.";

                case GREEN ->
                        "Your interest rate is in line with the market. "
                                + "It is a good level, but it may still be worth occasionally checking with your bank "
                                + "to ensure you are receiving their best available offer.";

                case YELLOW ->
                        "Your interest rate is slightly higher than current market levels. "
                                + "This could be a good opportunity to contact your bank and ask if they can improve your rate "
                                + "or match better offers.";

                case ORANGE ->
                        "Your interest rate is clearly higher than what many other customers are offered today. "
                                + "It may be worthwhile to negotiate or compare with other banks to see if you can secure a lower rate.";

                case RED ->
                        "Your interest rate is significantly higher than current market levels. "
                                + "You likely have a lot to gain by negotiating or comparing offers from multiple banks "
                                + "to find a better rate.";

                default ->
                        "We currently lack sufficient market data to provide a full recommendation.";
            };
        };
    }

    // =========================
    // FIXED RATE – ANALYSIS
    // =========================

    public String fixedAnalysisShortTerm(BigDecimal rate) {
        return switch (lang) {

            case SV ->
                    "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer. "
                            + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                            + "Du har en bunden ränta på " + rate + "%. Din bindningstid löper ut inom kort, "
                            + "vilket innebär att du snart kan välja ny ränta utan kostnad. "
                            + "Det är vanligt att man förhandlar räntan när det är mindre än en månad kvar av bindningstiden, "
                            + "men det kan vara smart att börja förbereda sig redan nu.\n\n"
                            + "Här visar vi marknadsläget just nu för att hjälpa dig inför ditt kommande bindningsval och ge en tydlig bild "
                            + "av vilka nivåer som är konkurrenskraftiga idag.";

            case EN ->
                    "Since your interest rate is fixed, it cannot be directly compared to current market levels. "
                            + "We therefore present an informational view to guide you ahead of your next fixed-rate decision.\n\n"
                            + "You currently have a fixed interest rate of " + rate + "%. Your fixed period is ending soon, "
                            + "which means you will shortly be able to choose a new rate without any cost. "
                            + "It is common to negotiate the interest rate when less than one month remains on the fixed period, "
                            + "but it can be wise to start preparing already now.\n\n"
                            + "Here we show the current market situation to help you prepare for your upcoming fixed-rate decision "
                            + "and give you a clear picture of which levels are competitive today.";
        };
    }

    public String fixedAnalysisLongTerm(BigDecimal rate) {
        return switch (lang) {

            case SV ->
                    "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer. "
                            + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                            + "Du har en bunden ränta på " + rate + "%. Det är lång tid kvar tills bindningstiden löper ut, "
                            + "vilket innebär att du normalt inte kan omförhandla eller byta ränta kostnadsfritt ännu. "
                            + "Om du vill undersöka möjligheten att byta i förtid kan du be din bank om en uppgift på eventuell "
                            + "ränteskillnadsersättning.\n\n"
                            + "Vi visar ändå hur ränteläget ser ut just nu, så att du redan nu får en bild av marknaden inför ditt nästa bindningsval.";

            case EN ->
                    "Since your interest rate is fixed, it cannot be directly compared to current market levels. "
                            + "We therefore present an informational view to guide you ahead of your next fixed-rate decision.\n\n"
                            + "You currently have a fixed interest rate of " + rate + "%. There is still a long time remaining on your fixed period, "
                            + "which means you normally cannot renegotiate or change your rate without cost at this time. "
                            + "If you want to explore the possibility of changing early, you can ask your bank about any potential "
                            + "interest rate compensation fees.\n\n"
                            + "We still show the current interest rate environment so you can already gain an understanding of the market "
                            + "ahead of your next fixed-rate decision.";
        };
    }

    public String fixedAnalysisVeryShort(BigDecimal rate) {
        return switch (lang) {

            case SV ->
                    "Eftersom din ränta är bunden går den inte att jämföra direkt med dagens marknadsnivåer. "
                            + "Vi visar därför ett informativt läge för att guida dig inför nästa bindningsval.\n\n"
                            + "Du har en bunden ränta på " + rate + "%. Din bindningstid löper ut mycket snart, "
                            + "vilket innebär att du nu befinner dig i ett optimalt läge att förhandla om en ny ränta.\n\n"
                            + "Här visar vi hur marknaden ser ut just nu för att hjälpa dig inför ditt kommande bindningsval.";

            case EN ->
                    "Since your interest rate is fixed, it cannot be directly compared to current market levels. "
                            + "We therefore present an informational view to guide you ahead of your next fixed-rate decision.\n\n"
                            + "You currently have a fixed interest rate of " + rate + "%. Your fixed period is ending very soon, "
                            + "which means you are now in an optimal position to negotiate a new interest rate.\n\n"
                            + "Here we show the current market situation to help you prepare for your upcoming fixed-rate decision.";
        };
    }

    // =========================
    // FIXED RATE – RECOMMENDATIONS
    // =========================

    public String fixedRecommendationShortTerm() {
        return switch (lang) {
            case SV -> "Eftersom din bindningstid snart löper ut är det ett bra läge att börja titta på olika alternativ "
                    + "och fundera på vilken bindningstid som passar dig bäst framöver.";
            case EN -> "Since your fixed term is about to expire, this is a good time to start reviewing different options "
                    + "and consider which fixed period suits you best going forward.";
        };
    }

    public String fixedRecommendationLongTerm() {
        return switch (lang) {
            case SV -> "Ett bra tillfälle att göra en ny räntekoll är när det är mindre än en månad kvar av bindningstiden. "
                    + "Du kan redan nu undersöka om ränteskillnadsersättningen är låg, men de flesta får bäst möjligheter "
                    + "att förhandla när bindningstiden närmar sig sitt slut.";
            case EN -> "A good time to review your interest rate is when there is less than one month left on the fixed term. "
                    + "You may already check whether the early repayment compensation is low, but most people get the best "
                    + "negotiation opportunities as the fixed period approaches its end.";
        };
    }

    public String fixedRecommendationVeryShort() {
        return switch (lang) {
            case SV -> "När det är mindre än en månad kvar av bindningstiden är det vanligt att påbörja ränteförhandling. "
                    + "Kontakta gärna banken för att höra vilka nivåer de kan erbjuda.";
            case EN -> "When there is less than one month left on the fixed term, it is common to start interest rate negotiations. "
                    + "You may want to contact your bank to see what rates they can offer.";
        };
    }

    // =========================
    // OFFER FLOW
    // =========================

    public String offerAnalysis(BigDecimal rate, BigDecimal diff, RateComparison comparison) {

        String diffText = diffLabel(diff, comparison);
        boolean worseThanMarket = comparison == RateComparison.HIGHER;

        return switch (lang) {
            case SV -> "Vi har analyserat ditt ränteerbjudande på " + rate + "%. "
                    + "Erbjudandet ligger " + diffText
                    + " jämfört med den lägsta aktuella snitträntan på marknaden. "
                    + "Det betyder att ditt erbjudande står sig "
                    + (worseThanMarket ? "sämre" : "bättre")
                    + " än vad många andra kunder får just nu.";

            case EN -> "We have analysed your interest rate offer of " + rate + "%. "
                    + "The offer is " + diffText
                    + " compared to the lowest average rate currently on the market. "
                    + "This means your offer compares "
                    + (worseThanMarket ? "worse" : "better")
                    + " than what many other customers receive today.";
        };
    }

    // =========================
    // PREFERENCE ADVICE
    // =========================

    public String preferenceAdvice(RatePreference pref) {

        if (pref == null) return "";

        return switch (lang) {

            case SV -> switch (pref) {

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

            case EN -> switch (pref) {

                case VARIABLE_3M ->
                        "A variable interest rate suits you if you prioritise flexibility and are comfortable with costs varying over time. "
                                + "It can both rise and fall, but gives you the freedom to switch banks or fix your rate when the timing feels right.";

                case SHORT ->
                        "Short fixed terms (1–3 years) suit you if you want some stability while keeping flexibility in the medium term. "
                                + "A good choice if you want to secure your costs for a period without locking in for too long.";

                case LONG ->
                        "Longer fixed terms (4–10 years) suit you if you want high predictability and protection against future rate increases. "
                                + "Keep in mind that flexibility is reduced and early repayment fees may apply if you exit the loan early.";
            };
        };
    }

    // =========================
    // DIFF LABEL HELPER
    // =========================
    // Converts a numeric rate difference and its direction into
    // a localized, human-readable description (SV / EN).
    // All language-specific wording for rate comparisons is centralized here.

    private String diffLabel(BigDecimal diff, RateComparison comparison) {

        if (diff == null || comparison == RateComparison.UNKNOWN) {
            return lang == Language.SV ? "okänt" : "unknown";
        }

        String value = diff.setScale(2, RoundingMode.HALF_UP) + "%";

        return switch (lang) {
            case SV -> switch (comparison) {
                case HIGHER -> value + " högre";
                case LOWER -> value + " lägre";
                case EQUAL -> "i nivå med";
                default -> "okänt";
            };
            case EN -> switch (comparison) {
                case HIGHER -> value + " higher";
                case LOWER -> value + " lower";
                case EQUAL -> "on par with";
                default -> "unknown";
            };
        };
    }
}