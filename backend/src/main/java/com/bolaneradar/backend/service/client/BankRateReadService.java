package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.BankRateRowDto;
import com.bolaneradar.backend.dto.mapper.api.BankRateMapper;
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
import java.util.stream.Collectors;

@Service
public class BankRateReadService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;

    private static final List<MortgageTerm> SORT_ORDER = List.of(
            MortgageTerm.VARIABLE_3M,
            MortgageTerm.FIXED_1Y,
            MortgageTerm.FIXED_2Y,
            MortgageTerm.FIXED_3Y,
            MortgageTerm.FIXED_4Y,
            MortgageTerm.FIXED_5Y,
            MortgageTerm.FIXED_6Y,
            MortgageTerm.FIXED_7Y,
            MortgageTerm.FIXED_8Y,
            MortgageTerm.FIXED_9Y,
            MortgageTerm.FIXED_10Y
    );

    public BankRateReadService(
            BankRepository bankRepository,
            MortgageRateRepository rateRepository
    ) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
    }

    // -------------------------------------------------------------
    //   HÄmtar aktuella räntor för en bank
    // -------------------------------------------------------------
    public Map<String, Object> getRatesForBank(String bankName) {

        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        // Hämta senaste månad med snitträntedata
        LocalDate latestAvgDate = rateRepository.findLatestAverageDateForBank(bank.getId());
        LocalDate latestMonth = latestAvgDate != null ? latestAvgDate.withDayOfMonth(1) : null;

        Map<MortgageTerm, List<MortgageRate>> grouped =
                bank.getMortgageRates().stream()
                        .collect(Collectors.groupingBy(MortgageRate::getTerm));

        List<BankRateRowDto> rows = new ArrayList<>();

        for (var entry : grouped.entrySet()) {

            MortgageTerm term = entry.getKey();
            String termCode = term.name();

            MortgageRate latestList =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.LISTRATE
                    );

            MortgageRate latestAvg = null;
            if (latestMonth != null) {
                latestAvg = rateRepository.findAverageRateForBankAndTermAndMonth(
                        bank.getId(), term, latestMonth, latestMonth.plusMonths(1)
                );
            }

            rows.add(BankRateMapper.toDto(termCode, latestList, latestAvg));
        }

        // Sortera efter logisk ordning
        rows.sort(Comparator.comparing(
                row -> SORT_ORDER.indexOf(MortgageTerm.valueOf(row.term()))
        ));

        // Bygg return-objekt
        Map<String, Object> result = new HashMap<>();
        result.put("month", latestMonth);
        result.put("monthFormatted", formatMonth(latestMonth));
        result.put("rows", rows);

        return result;
    }

    private String formatMonth(LocalDate date) {
        if (date == null) return null;
        return date.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("sv"))
                + " " + date.getYear();
    }
}