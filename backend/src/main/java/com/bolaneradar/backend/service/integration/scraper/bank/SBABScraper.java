package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hämtar SBAB:s bolåneräntor via deras publika JSON-API.
 * Inkluderar både aktuella listräntor och snitträntor.
 */
@Service
public class SBABScraper implements BankScraper {

    private static final String LISTRATE_URL =
            "https://www.sbab.se/api/interest-mortgage-service/api/v2/interest";
    private static final String AVERAGERATE_URL =
            "https://www.sbab.se/api/historical-average-interest-rate-service/interest-rate/average-interest-rate-last-twelve-months-by-period";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getBankName() {
        return "SBAB";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();
        rates.addAll(fetchListRates(bank));
        rates.addAll(fetchAverageRates(bank));
        ScraperUtils.logResult("SBAB", rates.size());
        return rates;
    }

    private List<MortgageRate> fetchListRates(Bank bank) throws IOException {
        List<MortgageRate> listRates = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LISTRATE_URL))
                .header("User-Agent", "BolåneRadar/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return listRates;

            JsonNode root = mapper.readTree(response.body()).path("listInterests");
            for (JsonNode item : root) {
                String period = item.path("interestPeriod").asText("");
                String rateString = item.path("interestRate").asText("");
                MortgageTerm term = mapToTerm(period);
                BigDecimal rate = ScraperUtils.parseRate(rateString);
                if (term != null && rate != null) {
                    listRates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return listRates;
    }

    private List<MortgageRate> fetchAverageRates(Bank bank) throws IOException {
        List<MortgageRate> list = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AVERAGERATE_URL))
                .header("User-Agent", "BolåneRadar/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return list;

            JsonNode latest = mapper.readTree(response.body())
                    .path("average_interest_rate_last_twelve_months")
                    .get(0);

            LocalDate date = parseDate(latest.path("period").asText(null));

            addIfPresent(list, bank, latest, "three_months", MortgageTerm.VARIABLE_3M, date);
            addIfPresent(list, bank, latest, "one_year", MortgageTerm.FIXED_1Y, date);
            addIfPresent(list, bank, latest, "two_years", MortgageTerm.FIXED_2Y, date);
            addIfPresent(list, bank, latest, "three_years", MortgageTerm.FIXED_3Y, date);
            addIfPresent(list, bank, latest, "four_years", MortgageTerm.FIXED_4Y, date);
            addIfPresent(list, bank, latest, "five_years", MortgageTerm.FIXED_5Y, date);
            addIfPresent(list, bank, latest, "seven_years", MortgageTerm.FIXED_7Y, date);
            addIfPresent(list, bank, latest, "ten_years", MortgageTerm.FIXED_10Y, date);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return list;
    }

    private void addIfPresent(List<MortgageRate> list, Bank bank, JsonNode node,
                              String field, MortgageTerm term, LocalDate date) {
        if (node.hasNonNull(field)) {
            BigDecimal rate = node.get(field).decimalValue();
            list.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, date));
        }
    }

    private MortgageTerm mapToTerm(String code) {
        return switch (code) {
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

    private LocalDate parseDate(String text) {
        if (text == null) return LocalDate.now();
        try {
            return text.length() == 7 ? LocalDate.parse(text + "-01") : LocalDate.parse(text);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}