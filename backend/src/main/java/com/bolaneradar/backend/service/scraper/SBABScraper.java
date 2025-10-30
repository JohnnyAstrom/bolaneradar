package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hämtar SBAB:s bolåneräntor via deras publika JSON-API.
 * Inkluderar både aktuella listräntor och senaste snitträntor (senaste månad).
 */
@Service
public class SBABScraper implements BankScraper {

    private static final String LISTRATE_URL =
            "https://www.sbab.se/api/interest-mortgage-service/api/v2/interest";
    private static final String AVERAGERATE_URL =
            "https://www.sbab.se/api/historical-average-interest-rate-service/interest-rate/average-interest-rate-last-twelve-months-by-period";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();
        rates.addAll(fetchListRates(bank));
        rates.addAll(fetchAverageRates(bank));

        System.out.println("SBAB: hittade totalt " + rates.size() + " räntor (LIST + AVERAGE).");
        return rates;
    }

    /** Hämtar listräntor */
    private List<MortgageRate> fetchListRates(Bank bank) throws IOException {
        List<MortgageRate> listRates = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LISTRATE_URL))
                .header("User-Agent", "BolåneRadar/1.0 (Examensprojekt)")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("SBAB: kunde inte hämta listräntor (status " + response.statusCode() + ")");
                return listRates;
            }

            JsonNode root = mapper.readTree(response.body()).path("listInterests");
            for (JsonNode item : root) {
                String period = item.path("interestPeriod").asText("");
                String rateString = item.path("interestRate").asText("");
                if (period.isEmpty() || rateString.isEmpty()) continue;

                MortgageTerm term = mapToMortgageTerm(period);
                if (term == null) continue;

                BigDecimal rate = new BigDecimal(rateString);
                listRates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
            }

            System.out.println("SBAB: hittade " + listRates.size() + " listräntor.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return listRates;
    }

    /** Hämtar snitträntor från senaste månaden */
    private List<MortgageRate> fetchAverageRates(Bank bank) throws IOException {
        List<MortgageRate> averageRates = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AVERAGERATE_URL))
                .header("User-Agent", "BolåneRadar/1.0 (Examensprojekt)")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("SBAB: kunde inte hämta snitträntor (status " + response.statusCode() + ")");
                return averageRates;
            }

            JsonNode root = mapper.readTree(response.body()).path("average_interest_rate_last_twelve_months");
            if (root.isArray() && !root.isEmpty()) {
                JsonNode latest = root.get(0); // Senaste månaden (överst i listan)

                addIfPresent(averageRates, bank, latest, "three_months", MortgageTerm.VARIABLE_3M);
                addIfPresent(averageRates, bank, latest, "one_year", MortgageTerm.FIXED_1Y);
                addIfPresent(averageRates, bank, latest, "two_years", MortgageTerm.FIXED_2Y);
                addIfPresent(averageRates, bank, latest, "three_years", MortgageTerm.FIXED_3Y);
                addIfPresent(averageRates, bank, latest, "four_years", MortgageTerm.FIXED_4Y);
                addIfPresent(averageRates, bank, latest, "five_years", MortgageTerm.FIXED_5Y);
                addIfPresent(averageRates, bank, latest, "seven_years", MortgageTerm.FIXED_7Y);
                addIfPresent(averageRates, bank, latest, "ten_years", MortgageTerm.FIXED_10Y);
            }

            System.out.println("SBAB: hittade " + averageRates.size() + " snitträntor.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return averageRates;
    }

    /** Hjälpmetod: lägg till ränta om värdet finns */
    private void addIfPresent(List<MortgageRate> list, Bank bank, JsonNode node, String field, MortgageTerm term) {
        if (node.hasNonNull(field)) {
            BigDecimal rate = node.get(field).decimalValue();
            list.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, LocalDate.now()));
        }
    }

    /** Mappning från period till enum */
    private MortgageTerm mapToMortgageTerm(String period) {
        return switch (period) {
            case "P_3_MONTHS" -> MortgageTerm.VARIABLE_3M;
            case "P_1_YEAR" -> MortgageTerm.FIXED_1Y;
            case "P_2_YEARS" -> MortgageTerm.FIXED_2Y;
            case "P_3_YEARS" -> MortgageTerm.FIXED_3Y;
            case "P_4_YEARS" -> MortgageTerm.FIXED_4Y;
            case "P_5_YEARS" -> MortgageTerm.FIXED_5Y;
            case "P_7_YEARS" -> MortgageTerm.FIXED_7Y;
            case "P_10_YEARS" -> MortgageTerm.FIXED_10Y;
            default -> null;
        };
    }
}
