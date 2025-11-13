package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service-lager för hantering av bolåneräntor.
 * Innehåller all logik mellan controller och repository
 * samt ansvarar för filtrering, sortering och beräkningar på räntedata.
 *
 * All kommunikation med databasen sker via MortgageRateRepository.
 * Service-lagret arbetar främst med entiteter men hanterar även DTO-objekt
 * vid skapande och konvertering av nya räntor.
 */
@Service
public class MortgageRateService {

    private final MortgageRateRepository mortgageRateRepository;
    private final BankService bankService;

    public MortgageRateService(MortgageRateRepository mortgageRateRepository, BankService bankService) {
        this.mortgageRateRepository = mortgageRateRepository;
        this.bankService = bankService;
    }

    // ---------------------------------------------------------------
    // Skapande och hämtning
    // ---------------------------------------------------------------

    /**
     * Skapar en ny bolåneränta baserat på inkommande DTO.
     * Service-lagret ansvarar för att säkerställa att angiven bank finns.
     */
    public MortgageRateDto createRate(MortgageRateDto dto) {
        var bank = bankService.getBankByName(dto.bankName())
                .orElseThrow(() -> new IllegalArgumentException("Banken finns inte: " + dto.bankName()));

        MortgageRate rate = MortgageRateMapper.toEntity(dto, bank);
        mortgageRateRepository.save(rate);
        return MortgageRateMapper.toDto(rate);
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
     */
    public List<MortgageRate> getAllRates() {
        return mortgageRateRepository.findAll();
    }

    /**
     * Hämtar alla bolåneräntor kopplade till en specifik bank.
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

    // ---------------------------------------------------------------
    // Hämtning av senaste räntor
    // ---------------------------------------------------------------

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
}
