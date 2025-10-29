package com.bolaneradar.backend.service;

import com.bolaneradar.backend.dto.BankHistoryDto;
import com.bolaneradar.backend.dto.LatestRateDto;
import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    public List<LatestRateDto> getLatestRatesPerBank() {
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
                .map(rate -> new LatestRateDto(
                        rate.getBank().getName(),
                        rate.getTerm().name(),
                        rate.getRateType().name(),
                        rate.getRatePercent(),
                        rate.getEffectiveDate()
                ))
                // Sortera: först per bank, sedan per term
                .sorted(Comparator
                        .comparing(LatestRateDto::bankName)
                        .thenComparing(dto -> sortOrder(dto.term())))
                .toList();
    }

    /**
     * Bestämmer sorteringsordningen för ränteterm.
     */
    private int sortOrder(String term) {
        return switch (term) {
            case "VARIABLE_3M" -> 1;
            case "FIXED_1Y" -> 2;
            case "FIXED_2Y" -> 3;
            case "FIXED_3Y" -> 4;
            case "FIXED_5Y" -> 5;
            default -> 99;
        };
    }

    /**
     * Hämtar hela historiken av räntor för en viss bank,
     * med valfri filtrering och sortering.
     */
    public List<LatestRateDto> getRateHistoryForBank(
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

        Comparator<MortgageRate> comparator = Comparator.comparing(MortgageRate::getEffectiveDate);
        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }
        rates = rates.stream().sorted(comparator).toList();


        return rates.stream()
                .map(rate -> new LatestRateDto(
                        bank.getName(),
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
                    List<LatestRateDto> rates = getRateHistoryForBank(bank, from, to, sort);
                    return new BankHistoryDto(bank.getName(), rates);
                })
                .toList();
    }
}