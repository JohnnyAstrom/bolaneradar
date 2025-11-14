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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Handelsbanken Scraper – JSON-baserad
 * ✔ Korrekt tolkning av periodBasisType för rörlig/bunden ränta
 * ✔ Hämtar 7 list-räntor
 * ✔ Hämtar 7 snitträntor men bara för SENASTE månaden
 * ✔ Ingen risk för duplicated 3Y/3M
 */
@Service
public class HandelsbankenScraper implements BankScraper {

    private static final String LIST_URL =
            "https://www.handelsbanken.se/tron/slana/slan/service/mortgagerates/v1/interestrates";

    private static final String AVERAGE_URL =
            "https://www.handelsbanken.se/tron/slana/slan/service/mortgagerates/v1/averagerates";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        rates.addAll(fetchListRates(bank));
        rates.addAll(fetchLatestAverageRates(bank));

        ScraperUtils.logResult("Handelsbanken", rates.size());
        return rates;
    }

    // ------------------------------------------------------------
    // 1. LIST-RÄNTOR
    // ------------------------------------------------------------
    private List<MortgageRate> fetchListRates(Bank bank) throws IOException {
        List<MortgageRate> list = new ArrayList<>();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(LIST_URL))
                .header("User-Agent", "BolåneRadar/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return list;

            JsonNode root = mapper.readTree(res.body()).path("interestRates");

            for (JsonNode item : root) {
                String term = item.path("term").asText();
                String basis = item.path("periodBasisType").asText();
                BigDecimal rate = item.path("rateValue").path("valueRaw").decimalValue();

                MortgageTerm mapped = mapTerm(term, basis);
                if (mapped != null) {
                    list.add(new MortgageRate(bank, mapped, RateType.LISTRATE, rate, LocalDate.now()));
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return list;
    }

    // ------------------------------------------------------------
    // 2. SNITTRÄNTOR (senaste månad)
    // ------------------------------------------------------------
    private List<MortgageRate> fetchLatestAverageRates(Bank bank) throws IOException {
        List<MortgageRate> list = new ArrayList<>();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AVERAGE_URL))
                .header("User-Agent", "BolåneRadar/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return list;

            JsonNode periods = mapper.readTree(res.body()).path("averageRatePeriods");

            // Hitta SENASTE period, exempel: "202510"
            YearMonth latest = null;
            JsonNode latestNode = null;

            for (JsonNode node : periods) {
                String p = node.path("period").asText();
                if (p == null || p.isBlank()) continue;

                YearMonth ym = YearMonth.parse(
                        p.substring(0, 4) + "-" + p.substring(4, 6)
                );

                if (latest == null || ym.isAfter(latest)) {
                    latest = ym;
                    latestNode = node;
                }
            }

            if (latestNode == null) return list;

            LocalDate effectiveDate = latest.atDay(1);

            for (JsonNode item : latestNode.path("rates")) {
                String term = item.path("term").asText();
                String basis = item.path("periodBasisType").asText();
                BigDecimal rate = item.path("rateValue").path("valueRaw").decimalValue();

                MortgageTerm mapped = mapTerm(term, basis);
                if (mapped != null) {
                    list.add(new MortgageRate(bank, mapped, RateType.AVERAGERATE, rate, effectiveDate));
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return list;
    }

    // ------------------------------------------------------------
    // 3. TERM-MAPPNING (helt korrekt enligt Handelsbankens JSON)
    // ------------------------------------------------------------
    private MortgageTerm mapTerm(String term, String basis) {

        // --- RÖRLIG RÄNTA ---
        // Handelsbanken: periodBasisType "3" betyder ALLTID rörlig 3 mån
        if ("3".equals(basis)) {
            return MortgageTerm.VARIABLE_3M;
        }

        // --- FAST RÄNTA ---
        // När basis = "4" används term som år
        return switch (term) {
            case "1"  -> MortgageTerm.FIXED_1Y;
            case "2"  -> MortgageTerm.FIXED_2Y;
            case "3"  -> MortgageTerm.FIXED_3Y;
            case "5"  -> MortgageTerm.FIXED_5Y;
            case "8"  -> MortgageTerm.FIXED_8Y;
            case "10" -> MortgageTerm.FIXED_10Y;
            default   -> null;
        };
    }
}