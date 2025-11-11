package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-lager för hantering av bolåneräntor.
 * Innehåller all logik mellan controller och repository
 * samt ansvarar för filtrering, sortering och beräkningar på räntedata.
 *
 * All kommunikation med databasen sker via MortgageRateRepository.
 * Service-lagret arbetar enbart med entiteter (inte DTO:er).
 */
@Service
public class MortgageRateService {

    private final MortgageRateRepository mortgageRateRepository;
    private final BankService bankService;

    public MortgageRateService(MortgageRateRepository mortgageRateRepository, BankService bankService) {
        this.mortgageRateRepository = mortgageRateRepository;
        this.bankService = bankService;
    }

    /**
     * Sparar en lista av bolåneräntor i databasen.
     * Används vid skapande av flera räntor samtidigt (t.ex. via import eller scraping).
     */
    public List<MortgageRate> saveAll(List<MortgageRate> rates) {
        return mortgageRateRepository.saveAll(rates);
    }

    /**
     * Hämtar alla bolåneräntor i databasen.
     * Returnerar en lista av MortgageRate-objekt.
     */
    public List<MortgageRate> getAllRates() {
        return mortgageRateRepository.findAll();
    }

    /**
     * Hämtar alla bolåneräntor kopplade till en specifik bank.
     * @param bank den bank vars räntor ska hämtas
     */
    public List<MortgageRate> getRatesByBank(Bank bank) {
        return mortgageRateRepository.findByBank(bank);
    }

    /**
     * Sparar en enskild bolåneränta i databasen.
     * Används främst vid enstaka uppdateringar.
     */
    public MortgageRate saveRate(MortgageRate rate) {
        return mortgageRateRepository.save(rate);
    }

    /**
     * Hämtar de senaste bolåneräntorna per bank och bindningstid
     * för en specifik räntetyp (LISTRATE eller AVERAGERATE).
     *
     * Sorterar resultatet alfabetiskt per bank och därefter per bindningstid.
     */
    public List<MortgageRate> getLatestRatesByType(RateType rateType) {
        return mortgageRateRepository.findLatestRatesByType(rateType)
                .stream()
                .sorted(Comparator
                        .comparing((MortgageRate r) -> r.getBank().getName())
                        .thenComparing(r -> sortOrder(r.getTerm().name()))
                )
                .toList();
    }

    /**
     * Hjälpmetod som bestämmer sorteringsordning för olika bindningstider.
     * Används för att visa räntor i logisk ordning (3M först, sedan 1 år, 2 år, osv.).
     */
    private int sortOrder(String term) {
        return switch (term) {
            case "VARIABLE_3M" -> 1;
            case "FIXED_1Y" -> 2;
            case "FIXED_2Y" -> 3;
            case "FIXED_3Y" -> 4;
            case "FIXED_4Y" -> 5;
            case "FIXED_5Y" -> 6;
            case "FIXED_6Y" -> 7;
            case "FIXED_7Y" -> 8;
            case "FIXED_8Y" -> 9;
            case "FIXED_9Y" -> 10;
            case "FIXED_10Y" -> 11;
            default -> 99;
        };
    }

    // ---------------------------------------------------------------
    // 🔹 Följande metoder används för mer avancerad analys och historik.
    // De kan på sikt flyttas till ett eget "RateAnalyticsService" om man vill
    // separera logik för beräkningar och trender.
    // ---------------------------------------------------------------

    /**
     * Hämtar historiska bolåneräntor för en viss bank,
     * grupperat per bindningstid (term) och räntetyp (rateType).
     *
     * Möjlighet finns att filtrera på term, rateType och datumintervall.
     */
    public List<MortgageRate> getRateHistoryForBank(
            Bank bank,
            LocalDate from,
            LocalDate to,
            String sort,
            RateType rateType,
            MortgageTerm term
    ) {
        List<MortgageRate> rates = mortgageRateRepository.findByBank(bank);

        // Filtrera på datumintervall
        if (from != null) {
            rates = rates.stream()
                    .filter(rate -> !rate.getEffectiveDate().isBefore(from))
                    .toList();
        }
        if (to != null) {
            rates = rates.stream()
                    .filter(rate -> !rate.getEffectiveDate().isAfter(to))
                    .toList();
        }

        // Filtrera på rateType och term (om angivna)
        if (rateType != null) {
            rates = rates.stream()
                    .filter(rate -> rate.getRateType() == rateType)
                    .toList();
        }
        if (term != null) {
            rates = rates.stream()
                    .filter(rate -> rate.getTerm() == term)
                    .toList();
        }

        // Sortering stigande eller fallande efter datum
        final String sortOrder = (sort == null || sort.isBlank()) ? "asc" : sort;
        final Comparator<MortgageRate> comparator =
                "desc".equalsIgnoreCase(sortOrder)
                        ? Comparator.comparing(MortgageRate::getEffectiveDate).reversed()
                        : Comparator.comparing(MortgageRate::getEffectiveDate);

        // Returnerar filtrerade och sorterade räntor
        return rates.stream().sorted(comparator).toList();
    }

    /**
     * Hämtar alla historiska räntor för flera banker inom valt intervall.
     * Används när man vill jämföra trender mellan flera banker.
     */
    public Map<String, List<MortgageRate>> getAllBanksRateHistory(
            List<Bank> banks,
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        return banks.stream()
                .collect(Collectors.toMap(
                        Bank::getName,
                        bank -> getRateHistoryForBank(bank, from, to, sort, null, null)
                ));
    }
}
