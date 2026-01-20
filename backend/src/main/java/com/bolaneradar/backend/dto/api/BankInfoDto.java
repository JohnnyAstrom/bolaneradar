package com.bolaneradar.backend.dto.api;

import java.util.List;

/**
 * DTO som innehåller fördjupad bankinformation
 * uppdelad per språk.
 * <p>
 * Används för informationssidor med längre
 * texter, FAQ och call-to-action.
 */
public class BankInfoDto {

    public Content sv;
    public Content en;

    public static class Content {
        public String intro;
        public List<Section> deepInsights;
        public List<FaqItem> faq;
        public String ctaLabel;
        public String ctaUrl;
    }

    public static class Section {
        public String heading;
        public String text;
    }

    public static class FaqItem {
        public String question;
        public String answer;
    }
}