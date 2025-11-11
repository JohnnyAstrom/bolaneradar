package com.bolaneradar.backend.service.integration.scraper;

import com.bolaneradar.backend.entity.*;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Webbskrapare för Ikano Bank.
 * Hämtar aktuella (listräntor) via JSON-API och senaste snitträntor från HTML-tabell.
 */
@Service
public class IkanoBankScraper implements BankScraper {

    private static final String API_URL = "https://ikanobank.se/api/econansforeteller/gettabledata";
    private static final String SNITT_URL = "https://ikanobank.se/bolan/bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        System.out.println("🏦 Startar skrapning för Ikano Bank ...");
        List<MortgageRate> rates = new ArrayList<>();

        try {
            // === 1️⃣ Listräntor via JSON-API ===
            @SuppressWarnings("unchecked")
            var dataList = (List<Map<String, Object>>) Objects.requireNonNull(new RestTemplate()
                            .getForObject(API_URL, Map.class))
                    .get("dataList");

            var grouped = dataList.stream()
                    .collect(Collectors.toMap(
                            i -> toInt(i.get("fixationPeriod")),
                            i -> i,
                            (a, b) -> toDouble(a.get("ltvGroupMinLtv")) <= toDouble(b.get("ltvGroupMinLtv")) ? a : b
                    ));

            grouped.keySet().stream().sorted().forEach(months -> {
                var i = grouped.get(months);
                var term = toTerm(months);
                if (term == null) return;

                double rate = toDouble(i.get("listPrice"));
                rates.add(new MortgageRate(bank, term, RateType.LISTRATE, BigDecimal.valueOf(rate), LocalDate.now()));
                System.out.printf("→ LISTRATE %s %.2f%%%n", term, rate);
            });

            // === 2️⃣ Snitträntor via HTML ===
            Document doc = Jsoup.connect(SNITT_URL)
                    .userAgent("Mozilla/5.0").timeout(10000).get();

            Elements rows = doc.select("table:last-of-type tbody tr");
            if (rows.isEmpty()) {
                System.out.println("⚠️ Hittade ingen snitträntetabell.");
            } else {
                // 🟢 Ta sista raden (senaste månad)
                Element lastRow = rows.last();
                Elements cols = lastRow.select("td");

                // Datum (första kolumnen, t.ex. "2025 10")
                LocalDate date = parseMonthColumn(cols.get(0).text());

                // Gå igenom räntorna per bindningstid
                for (int i = 1; i < cols.size(); i++) {
                    BigDecimal rate = ScraperUtils.parseRate(cols.get(i).text());
                    MortgageTerm term = ScraperUtils.parseTerm(getTermFromIndex(i));
                    if (term != null && rate != null) {
                        rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, date));
                        System.out.printf("→ AVERAGERATE %s = %.2f%% (%s)%n", term, rate, date);
                    }
                }
                System.out.println("Snitträntor hämtade för " + date + ".");
            }

        } catch (Exception e) {
            System.err.println("Fel vid Ikano Bank-scraping: " + e.getMessage());
        }

        System.out.println("🏁 Ikano Bank: totalt " + rates.size() + " räntor hittade.");
        return rates;
    }

    // --- Hjälpmetoder ---
    private static int toInt(Object o) {
        try { return o instanceof Number ? ((Number) o).intValue() : Integer.parseInt(o.toString()); }
        catch (Exception e) { return -1; }
    }

    private static double toDouble(Object o) {
        try { return o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble(o.toString()); }
        catch (Exception e) { return 0; }
    }

    private static MortgageTerm toTerm(int m) {
        return switch (m) {
            case 3 -> MortgageTerm.VARIABLE_3M;
            case 12 -> MortgageTerm.FIXED_1Y;
            case 24 -> MortgageTerm.FIXED_2Y;
            case 36 -> MortgageTerm.FIXED_3Y;
            case 48 -> MortgageTerm.FIXED_4Y;
            case 60 -> MortgageTerm.FIXED_5Y;
            case 84 -> MortgageTerm.FIXED_7Y;
            case 120 -> MortgageTerm.FIXED_10Y;
            default -> null;
        };
    }

    private static String getTermFromIndex(int i) {
        return switch (i) {
            case 1 -> "3 mån"; case 2 -> "1 år"; case 3 -> "2 år";
            case 4 -> "3 år"; case 5 -> "4 år"; case 6 -> "5 år";
            case 7 -> "7 år"; case 8 -> "10 år"; default -> null;
        };
    }

    /** Konverterar t.ex. "2025 10" → LocalDate(2025-10-01) */
    private static LocalDate parseMonthColumn(String text) {
        try {
            String[] parts = text.trim().split("\\s+");
            if (parts.length < 2) return LocalDate.now();
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return LocalDate.of(year, month, 1);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}