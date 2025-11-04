package com.bolaneradar.backend.service;

import com.bolaneradar.backend.dto.BankHistoryDto;
import com.bolaneradar.backend.dto.MortgageRateDto;
import com.bolaneradar.backend.dto.RateTrendDto;
import com.bolaneradar.backend.dto.RateRequestDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MortgageRateService {

    private final MortgageRateRepository mortgageRateRepository;
    private final BankService bankService;

    public MortgageRateService(MortgageRateRepository mortgageRateRepository, BankService bankService) {
        this.mortgageRateRepository = mortgageRateRepository;
        this.bankService = bankService;
    }

    /**
     * 🆕 Skapar en eller flera nya räntor baserat på inkommande DTO-lista.
     */
    public List<MortgageRateDto> createRates(List<RateRequestDto> requests) {
        List<MortgageRateDto> savedRates = new ArrayList<>();

        for (RateRequestDto request : requests) {
            Optional<Bank> optionalBank = bankService.getBankByName(request.bankName());
            if (optionalBank.isEmpty()) {
                System.err.println("Ingen bank hittades med namn: " + request.bankName());
                continue;
            }

            Bank bank = optionalBank.get();

            MortgageRate rate = new MortgageRate();
            rate.setBank(bank);
            rate.setTerm(request.term());
            rate.setRateType(request.rateType());
            rate.setRatePercent(request.ratePercent());
            rate.setEffectiveDate(request.effectiveDate());
            rate.setRateChange(request.rateChange());
            rate.setLastChangedDate(request.lastChangedDate());

            MortgageRate saved = mortgageRateRepository.save(rate);
            savedRates.add(MortgageRateMapper.toDto(saved));
        }

        return savedRates;
    }

    /** Hämtar alla bolåneräntor som DTO-objekt. */
    public List<MortgageRateDto> getAllRatesAsDto() {
        return mortgageRateRepository.findAll()
                .stream()
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    /** Hämta alla räntor kopplade till en specifik bank. */
    public List<MortgageRate> getRatesByBank(Bank bank) {
        return mortgageRateRepository.findByBank(bank);
    }

    /** Spara en ny bolåneränta i databasen. */
    public MortgageRate saveRate(MortgageRate rate) {
        return mortgageRateRepository.save(rate);
    }

    /**
     * Hämtar de senaste bolåneräntorna per bank och bindningstid
     * för en specifik räntetyp (LISTRATE eller AVERAGERATE).
     */
    public List<MortgageRateDto> getLatestRatesByType(RateType rateType) {
        return mortgageRateRepository.findLatestRatesByType(rateType)
                .stream()
                .sorted(Comparator
                        .comparing((MortgageRate r) -> r.getBank().getName())
                        .thenComparing(r -> sortOrder(r.getTerm().name()))
                )
                .map(MortgageRateMapper::toDto)
                .toList();
    }

    /** Bestämmer sorteringsordningen för ränteterm. */
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

    /** Hämtar hela historiken av räntor för en viss bank. */
    public List<MortgageRateDto> getRateHistoryForBank(
            Bank bank,
            LocalDate from,
            LocalDate to,
            String sort
    ) {
        List<MortgageRate> rates = mortgageRateRepository.findByBank(bank);

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

        if (sort == null || sort.isBlank()) {
            sort = "desc";
        }

        Comparator<MortgageRate> comparator = Comparator
                .comparing((MortgageRate r) -> r.getBank().getName())
                .thenComparing(MortgageRate::getEffectiveDate)
                .thenComparing(r -> sortOrder(r.getTerm().name()));

        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }

        rates = rates.stream().sorted(comparator).toList();

        return rates.stream()
                .map(rate -> new MortgageRateDto(
                        rate.getId(),
                        rate.getBank().getName(),
                        rate.getTerm(),
                        rate.getRateType(),
                        rate.getRatePercent(),
                        rate.getEffectiveDate(),
                        rate.getRateChange(),
                        rate.getLastChangedDate()
                ))
                .toList();
    }

    /** Hämtar historiska bolåneräntor för alla banker. */
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

    /** Beräknar förändringen i bolåneräntor mellan två mättillfällen. */
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

        List<MortgageRate> latestRates = mortgageRateRepository.findByEffectiveDate(to);
        List<MortgageRate> previousRates = mortgageRateRepository.findByEffectiveDate(from);

        if (rateType != null && !rateType.isBlank()) {
            latestRates = latestRates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
            previousRates = previousRates.stream()
                    .filter(r -> r.getRateType().name().equalsIgnoreCase(rateType))
                    .toList();
        }

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

        // sortera banker stigande, terminer enligt sortOrder, och förändring fallande
        trends.sort(
                Comparator.comparing(RateTrendDto::bankName)
                        .thenComparing(dto -> sortOrder(dto.term()))
                        .thenComparing(RateTrendDto::rateType)
                        .thenComparing(RateTrendDto::change, Comparator.reverseOrder())
        );

        return trends;
    }

    /** Beräknar alla förändringar i bolåneräntor inom ett valt tidsintervall. */
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

        // banker stigande, terminer enligt sortOrder, förändring fallande
        allTrends.sort(
                Comparator.comparing(RateTrendDto::bankName)
                        .thenComparing(dto -> sortOrder(dto.term()))
                        .thenComparing(RateTrendDto::rateType)
                        .thenComparing(RateTrendDto::change, Comparator.reverseOrder())
        );

        return allTrends;
    }
}
