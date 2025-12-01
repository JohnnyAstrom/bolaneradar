package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.MortgageRateComparisonDto;
import com.bolaneradar.backend.dto.mapper.api.MortgageRateComparisonMapper;
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
     * Huvudfunktion som hämtar och bygger data för räntetabellen.
     */
    public Map<String, Object> getComparisonDataFull(String termCode) {

        // 1. Översätt "3m" → MortgageTerm.VARIABLE_3M
        MortgageTerm term = mapTermCode(termCode);

        // 2. Hämta alla banker
        List<Bank> banks = bankRepository.findAll();

        // 3. Förbered variabler som ska fyllas i loopen
        List<MortgageRateComparisonDto> rows = new ArrayList<>();
        LocalDate commonMonth = null;

        // ============================================================
        // 4. Hämta data per bank
        // ============================================================
        for (Bank bank : banks) {

            // a) Senaste listränta
            MortgageRate latestListRate =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.LISTRATE
                    );

            // b) Senaste snittränta
            MortgageRate latestAverageRate =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.AVERAGERATE
                    );

            // c) Sätt gemensam snitträntemånad (för rubriken)
            if (latestAverageRate != null && commonMonth == null) {
                commonMonth = latestAverageRate.getEffectiveDate().withDayOfMonth(1);
            }

            // d) Hämta diff och lastChanged
            Double diff = extractDiff(latestListRate);
            LocalDate lastChanged = extractLastChanged(latestListRate);

            // e) Bygg radens DTO
            MortgageRateComparisonDto dto =
                    MortgageRateComparisonMapper.toDto(
                            bank.getName(),
                            latestListRate,
                            latestAverageRate,
                            diff,
                            lastChanged
                    );

            rows.add(dto);
        }

        // ============================================================
        // 5. Bygg responsobjektet till frontend
        // ============================================================
        Map<String, Object> result = new HashMap<>();

        result.put("averageMonth", commonMonth);
        result.put("averageMonthFormatted", formatMonth(commonMonth));
        result.put("rows", rows);

        return result;
    }

    // ------------------------------------------------------------
    // Hjälpfunktioner (gör huvudloopen mycket lättare att läsa)
    // ------------------------------------------------------------

    private MortgageTerm mapTermCode(String code) {
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

    private Double extractDiff(MortgageRate listRate)
    {
        if (listRate == null)
        {
            return null;
        }

        if (listRate.getRateChange() == null)
        {
            return null;
        }

        return listRate.getRateChange().doubleValue();
    }


    private LocalDate extractLastChanged(MortgageRate listRate)
    {
        if (listRate == null)
        {
            return null;
        }

        if (listRate.getLastChangedDate() == null)
        {
            return null;
        }

        return listRate.getLastChangedDate();
    }

    private String formatMonth(LocalDate date)
    {
        if (date == null)
        {
            return null;
        }

        String monthName = date
                .getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("sv"));

        return monthName + " " + date.getYear();
    }
}
