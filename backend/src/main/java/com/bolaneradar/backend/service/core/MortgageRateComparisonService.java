package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.MortgageRateComparisonDto;
import com.bolaneradar.backend.dto.mapper.MortgageRateComparisonMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class MortgageRateComparisonService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;

    public MortgageRateComparisonService(
            BankRepository bankRepository,
            MortgageRateRepository rateRepository
    ) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
    }

    /**
     * Returnerar ett objekt som innehåller:
     *   - averageMonth (YYYY-MM-01)
     *   - averageMonthFormatted ("okt 2025")
     *   - rows (lista av bankradsdata)
     */
    public Map<String, Object> getComparisonDataFull(String termCode) {

        MortgageTerm term = mapToTerm(termCode);

        List<Bank> banks = bankRepository.findAll();

        List<MortgageRateComparisonDto> rows = new ArrayList<>();

        LocalDate averageMonth = null;

        for (Bank bank : banks) {

            // Hämta senaste LIST
            MortgageRate listRate =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.LISTRATE
                    );

            // Hämta senaste AVERAGE
            MortgageRate avgRate =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.AVERAGERATE
                    );

            // Spara vilken månad snitträntan gäller
            if (avgRate != null && averageMonth == null) {
                averageMonth = avgRate.getEffectiveDate().withDayOfMonth(1);
            }

            // Diff & lastChanged direkt från databasen
            Double diff = listRate != null && listRate.getRateChange() != null
                    ? listRate.getRateChange().doubleValue()
                    : null;

            LocalDate lastChanged = listRate != null
                    ? listRate.getLastChangedDate()
                    : null;

            rows.add(MortgageRateComparisonMapper.toDto(
                    bank.getName(),
                    listRate,
                    avgRate,
                    diff,
                    lastChanged
            ));
        }

        // Format: "okt 2025"
        String formattedMonth = null;
        if (averageMonth != null) {
            formattedMonth = averageMonth
                    .getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("sv"))
                    + " " + averageMonth.getYear();
        }

        // Skapa response-objekt till frontend
        Map<String, Object> response = new HashMap<>();
        response.put("averageMonth", averageMonth != null ? averageMonth.toString() : null);
        response.put("averageMonthFormatted", formattedMonth);
        response.put("rows", rows);

        return response;
    }


    private MortgageTerm mapToTerm(String code) {
        return switch (code.toLowerCase()) {
            case "3m" -> MortgageTerm.VARIABLE_3M;
            case "1y" -> MortgageTerm.FIXED_1Y;
            case "2y" -> MortgageTerm.FIXED_2Y;
            case "3y" -> MortgageTerm.FIXED_3Y;
            case "4y" -> MortgageTerm.FIXED_4Y;
            case "5y" -> MortgageTerm.FIXED_5Y;
            case "7y" -> MortgageTerm.FIXED_7Y;
            case "10y" -> MortgageTerm.FIXED_10Y;
            default -> throw new IllegalArgumentException("Unknown mortgage term: " + code);
        };
    }
}