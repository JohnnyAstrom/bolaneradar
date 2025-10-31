package com.bolaneradar.backend.service;

import com.bolaneradar.backend.dto.BankHistoryDto;
import com.bolaneradar.backend.dto.RateTrendDto;
import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MortgageRateService {

    private final MortgageRateRepository mortgageRateRepository;

    // Konstruktorinjektion – Spring sköter kopplingen
    public MortgageRateService(MortgageRateRepository mortgageRateRepository) {
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Hämta alla bolåneräntor.
     */
    public List<MortgageRate> getAllRates() {
        return mortgageRateRepository.findAll();
    }

    /**
     * Hämtar alla bolåneräntor som DTO-objekt.
     * Inkluderar bankens namn men inte hela bankobjektet.
     */
    public List<MortgageRateDto> getAllRatesAsDto() {
        return mortgageRateRepository.findAll()
                .stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    /**
     * Hämta alla räntor kopplade till en specifik bank.
     */
    public List<MortgageRate> getRatesByBank(Bank bank) {
        return mortgageRateRepository.findByBank(bank);
    }

    /**
     * Spara en ny bolåneränta i databasen.
     * Om banken redan finns kopplas räntan dit.
     */
    public MortgageRate saveRate(MortgageRate rate) {
        return mortgageRateRepository.save(rate);
    }

    /**
     * Ta bort en ränta baserat på ID (för framtida användning).
     */
    public void deleteRate(Long id) {
        mortgageRateRepository.deleteById(id);
    }

    /**
     * Hämtar den senaste räntan per bank OCH term (bindningstid),
     * så att varje bank får sin senaste ränta för varje term, även om
     * olika bindningstider har uppdaterats vid olika datum.
     */
    public List<MortgageRateDto> getLatestRatesPerBank() {
        List<MortgageRate> allRates = mortgageRateRepository.findAll();

        return allRates.stream()
                // Gruppar efter kombinationen (bank + term)
                .collect(Collectors.groupingBy(
                        rate -> rate.getBank().getName() + "-" + rate.getTerm().name(),
                        Collectors.maxBy(Comparator
                                .comparing(MortgageRate::getEffectiveDate)
                                .thenComparing(MortgageRate::getId))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                // Mappa till DTO (för frontend)
                .map(rate -> new MortgageRateDto(
                        rate.getId(),
                        rate.getBank().getName(),
                        rate.getTerm().name(),
                        rate.getRateType().name(),
                        rate.getRatePercent(),
                        rate.getEffectiveDate()
                ))
                // Sortera: först per bank, sedan per term
                .sorted(Comparator
                        .comparing(MortgageRateDto::bankName)
                        .thenComparing(dto -> sortOrder(dto.term())))
                .toList();
    }

    /**
     * Bestämmer sorteringsordningen för ränteterm.
     * Lägre siffra = kommer först i listan.
     */
    private int sortOrder(String term) {
        return switch (term) {
            case "VARIABLE_3M" -> 1;  // Rörlig ränta (3 månader)
            case "FIXED_1Y" -> 2;
            case "FIXED_2Y" -> 3;
            case "FIXED_3Y" -> 4;
            case "FIXED_4Y" -> 5;
            case "FIXED_5Y" -> 6;
            case "FIXED_6Y" -> 7;
            case "FIXED_7Y" -> 8;
            case "FIXED_8Y" -> 9;
            case "FIXED_10Y" -> 10;
            default -> 99;  // okända termer hamnar sist
        };
    }

    /**
     * Hämtar hela historiken av räntor för en viss bank,
     * med valfri filtrering och sortering.
     */
    public List<MortgageRateDto> getRateHistoryForBank(
            Bank bank,
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        List<MortgageRate> rates = mortgageRateRepository.findByBank(bank);

        // Filtrering
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

        // Sortering
        if (sort == null || sort.isBlank()) {
            sort = "desc";
        }

        Comparator<MortgageRate> comparator = Comparator
                .comparing(MortgageRate::getEffectiveDate)
                .thenComparing(rate -> sortOrder(rate.getTerm().name()));

        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }

        rates = rates.stream().sorted(comparator).toList();

        return rates.stream()
                .map(rate -> new MortgageRateDto(
                        rate.getId(),
                        rate.getBank().getName(),
                        rate.getTerm().name(),
                        rate.getRateType().name(),
                        rate.getRatePercent(),
                        rate.getEffectiveDate()
                ))
                .toList();
    }

    /**
     * Hämtar historiska bolåneräntor för alla banker.
     * Varje bank returneras tillsammans med sina räntor,
     * med valfri filtrering och sortering.
     */
    public List<BankHistoryDto> getAllBanksRateHistory(
            List<Bank> banks,
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        return banks.stream()
                .map(bank -> {
                    List<MortgageRateDto> rates = getRateHistoryForBank(bank, from, to, sort);
                    return new BankHistoryDto(bank.getName(), rates);
                })
                .toList();
    }

    /**
     * Beräknar förändringen i bolåneräntor mellan två valfria mättillfällen.
     * <p>
     * Om parametrarna {@code from} och {@code to} inte anges används de två senaste datumen
     * som finns i databasen. Resultatet innehåller en post per bank, bindningstid och räntetyp
     * som visar skillnaden i räntenivå mellan dessa två mättillfällen.
     */
    public List<RateTrendDto> getRateTrends(LocalDate from, LocalDate to, String rateType) {
        List<LocalDate> dates = mortgageRateRepository.findDistinctEffectiveDatesDesc();

        if (from == null || to == null) {
            if (dates.size() < 2) {
                System.out.println("Inte tillräckligt många mättillfällen för att beräkna trender.");
                return Collections.emptyList();
            }
            to = dates.get(0);
            from = dates.get(1);
        }

        System.out.println("Jämför datum: " + from + " → " + to);

        List<MortgageRate> latestRates = mortgageRateRepository.findByEffectiveDate(to);
        List<MortgageRate> previousRates = mortgageRateRepository.findByEffectiveDate(from);

        // Filtrera på rateType om parameter finns
        if (rateType != null && !rateType.isBlank()) {
            latestRates = latestRates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
            previousRates = previousRates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
        }

        // Mappa tidigare räntor
        Map<String, Double> previousMap = previousRates.stream()
                .collect(Collectors.toMap(
                        r -> r.getBank().getName() + "_" + r.getTerm() + "_" + r.getRateType(),
                        r -> r.getRatePercent().doubleValue(),
                        (a, b) -> b
                ));

        List<RateTrendDto> trends = new ArrayList<>();

        for (MortgageRate rate : latestRates) {
            String key = rate.getBank().getName() + "_" + rate.getTerm() + "_" + rate.getRateType();
            Double prev = previousMap.get(key);
            if (prev != null) {
                trends.add(new RateTrendDto(
                        rate.getBank().getName(),
                        rate.getTerm().name(),
                        rate.getRateType().name(),
                        prev,
                        rate.getRatePercent().doubleValue(),
                        from,
                        to
                ));
            }
        }

        // Sortera – per bank, term, typ och förändring
        trends.sort(Comparator
                .comparing(RateTrendDto::bankName)
                .thenComparing(dto -> sortOrder(dto.term()))
                .thenComparing(RateTrendDto::rateType)
                .thenComparingDouble(RateTrendDto::change)
                .reversed());

        System.out.println("Beräknade " + trends.size() + " trendposter mellan " + from + " och " + to);
        return trends;
    }

    /**
     * Beräknar alla förändringar i bolåneräntor inom ett valt tidsintervall.
     * Inkluderar även oförändrade dagar för en komplett översikt.
     */
    public List<RateTrendDto> getRateTrendsInRange(LocalDate from, LocalDate to, String rateType) {
        List<MortgageRate> rates = mortgageRateRepository.findByEffectiveDateBetween(from, to);

        if (rateType != null && !rateType.isBlank()) {
            rates = rates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
        }

        Map<String, List<MortgageRate>> grouped = rates.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getBank().getName() + "_" + r.getTerm() + "_" + r.getRateType()
                ));

        List<RateTrendDto> allTrends = new ArrayList<>();

        for (List<MortgageRate> group : grouped.values()) {
            group.sort(Comparator.comparing(MortgageRate::getEffectiveDate));

            for (int i = 0; i < group.size() - 1; i++) {
                MortgageRate prev = group.get(i);
                MortgageRate next = group.get(i + 1);

                double previousRate = prev.getRatePercent().doubleValue();
                double currentRate = next.getRatePercent().doubleValue();
                double change = currentRate - previousRate;

                double roundedChange = Math.round(change * 100.0) / 100.0;

                allTrends.add(new RateTrendDto(
                        prev.getBank().getName(),
                        prev.getTerm().name(),
                        prev.getRateType().name(),
                        previousRate,
                        currentRate,
                        prev.getEffectiveDate(),
                        next.getEffectiveDate(),
                        roundedChange
                ));
            }
        }

        // Sortera resultatet efter bank, term, typ och störst förändring
        allTrends.sort(Comparator
                .comparing(RateTrendDto::bankName)
                .thenComparing(dto -> sortOrder(dto.term()))
                .thenComparing(RateTrendDto::rateType)
                .thenComparingDouble(RateTrendDto::change)
                .reversed());

        System.out.println("Beräknade " + allTrends.size() +
                " trendposter mellan " + from + " och " + to);

        return allTrends;
    }
}