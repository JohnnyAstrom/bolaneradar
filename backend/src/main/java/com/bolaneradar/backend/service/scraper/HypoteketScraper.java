package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Webbskrapare för Hypoteket.
 * Hämtar aktuella bolåneräntor (motsvarande listräntor)
 * via Hypotekets publika API med hög belåningsgrad (~65%).
 *
 * Hypoteket redovisar inte snitträntor i maskinläsbart format,
 * utan endast i grafisk form på webbplatsen.
 */
@Service
public class HypoteketScraper implements BankScraper {

    // Publikt API som ger aktuella räntor vid hög belåningsgrad (ca 65%)
    private static final String API_URL =
            "https://api.hypoteket.com/api/v1/loans/interestRates?loanSize=200000&propertyValue=310000";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) throws IOException {
        List<MortgageRate> rates = new ArrayList<>();

        // Skapa HTTP-anslutning
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Encoding", "identity"); // be om okomprimerad data
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        // Kontrollera svarskod
        int status = connection.getResponseCode();
        System.out.println("Hypoteket API-status: " + status);
        if (status != 200) {
            System.out.println("Hypoteket: ogiltig statuskod " + status);
            return rates;
        }

        // Läs svaret (hantera gzip om det behövs)
        InputStream inputStream;
        String encoding = connection.getContentEncoding();
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            inputStream = new GZIPInputStream(connection.getInputStream());
            System.out.println("Hypoteket-svar var gzip-komprimerat — dekomprimerar...");
        } else {
            inputStream = connection.getInputStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        String json = response.toString();
        System.out.println("Hypoteket rådata (första 200 tecken): " +
                json.substring(0, Math.min(json.length(), 200)));

        if (json.isEmpty() || json.equals("[]")) {
            System.out.println("Hypoteket: tomt svar.");
            return rates;
        }

        // Tolka JSON och skapa MortgageRate-objekt
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        for (JsonNode node : root) {
            String termText = node.path("interestTerm").asText("").toLowerCase();
            if (termText.isEmpty()) continue;

            // Använd codeInterestRate (motsvarar Hypotekets listränta)
            BigDecimal rate = null;
            if (node.has("codeInterestRate")) {
                rate = node.get("codeInterestRate").decimalValue();
            } else if (node.has("codeEffectiveInterestRate")) {
                rate = node.get("codeEffectiveInterestRate").decimalValue();
            } else if (node.has("effectiveInterestRate")) {
                rate = node.get("effectiveInterestRate").decimalValue();
            } else if (node.has("rate")) {
                rate = node.get("rate").decimalValue();
            }

            // Identifiera bindningstid
            MortgageTerm term = ScraperUtils.parseTerm(termText);
            if (term == null || rate == null) continue;

            rates.add(new MortgageRate(bank, term, RateType.LISTRATE, rate, LocalDate.now()));
        }

        // Logga resultat
        if (rates.isEmpty()) {
            System.out.println("Hypoteket: inga giltiga räntor hittades.");
        } else {
            System.out.println("Hypoteket: hittade " + rates.size() + " räntor (listräntor).");
            rates.forEach(r -> System.out.println(" - " + r.getTerm() + ": " + r.getRatePercent() + "%"));
        }

        return rates;
    }
}