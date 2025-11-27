package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.BankRateRowDto;
import com.bolaneradar.backend.dto.mapper.BankRateMapper;
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
public class BankRateService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;

    /**
     * Sorteringsordning för bindningstider.
     * Används för att visa räntorna i logisk ordning.
     */
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

    public BankRateService(
            BankRepository bankRepository,
            MortgageRateRepository rateRepository
    ) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
    }

    // ========================================================================
    // =====================   HÄMTA RÄNTOR PER BANK   ========================
    // ========================================================================

    /**
     * Hämtar alla aktuella räntor (list och avg) för en specifik bank.
     *
     * Logik:
     *  1. Hitta bank (case-insensitive)
     *  2. Hämta bankens senaste snitträntemånad
     *  3. För varje bindningstid:
     *       - hämta senaste listränta
     *       - hämta snittränta endast om den finns i samma månad
     *  4. Sortera rader i logisk ordning
     *
     * Returnerar:
     *  - En lista med BankRateRowDto
     *  - En månad som frontend kan visa som "Snittränta (okt 2025)"
     */
    public Map<String, Object> getRatesForBank(String bankName) {

        // 1. Hitta bank
        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        // 2. Hitta bankens senaste snitträntedatum
        LocalDate latestAvgDate = rateRepository.findLatestAverageDateForBank(bank.getId());
        LocalDate latestMonth = latestAvgDate != null ? latestAvgDate.withDayOfMonth(1) : null;

        // Grupp: term → alla räntor som hör till term
        Map<MortgageTerm, List<MortgageRate>> grouped =
                bank.getMortgageRates().stream()
                        .collect(Collectors.groupingBy(MortgageRate::getTerm));

        List<BankRateRowDto> rows = new ArrayList<>();

        // ============================================================
        // 3. Bygg rader per bindningstid
        // ============================================================
        for (var entry : grouped.entrySet()) {

            MortgageTerm term = entry.getKey();
            String termLabel = toTermLabel(term);

            // a) Hämta senaste listränta
            MortgageRate latestList =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.LISTRATE
                    );

            // b) Hämta snittränta för rätt månad (om månad finns)
            MortgageRate latestAvg = null;

            if (latestMonth != null) {
                latestAvg = rateRepository.findAverageRateForBankAndTermAndMonth(
                        bank.getId(),
                        term,
                        latestMonth,
                        latestMonth.plusMonths(1)
                );
            }

            // c) Lägg till rad
            rows.add(BankRateMapper.toDto(termLabel, latestList, latestAvg));
        }

        // ============================================================
        // 4. Sortera raderna i logisk ordning
        // ============================================================
        rows.sort(Comparator.comparing(
                row -> SORT_ORDER.indexOf(mapLabelToTerm(row.term()))
        ));

        // ============================================================
        // 5. Bygg respons
        // ============================================================
        Map<String, Object> result = new HashMap<>();
        result.put("month", latestMonth);
        result.put("monthFormatted", formatMonth(latestMonth));
        result.put("rows", rows);

        return result;
    }

    // ========================================================================
    // =======================   HJÄLPFUNKTIONER   ============================
    // ========================================================================

    /**
     * Översätter "3 mån", "1 år" etc → enum.
     */
    private MortgageTerm mapLabelToTerm(String label) {
        return switch (label) {
            case "3 mån" -> MortgageTerm.VARIABLE_3M;
            case "1 år" -> MortgageTerm.FIXED_1Y;
            case "2 år" -> MortgageTerm.FIXED_2Y;
            case "3 år" -> MortgageTerm.FIXED_3Y;
            case "4 år" -> MortgageTerm.FIXED_4Y;
            case "5 år" -> MortgageTerm.FIXED_5Y;
            case "6 år" -> MortgageTerm.FIXED_6Y;
            case "7 år" -> MortgageTerm.FIXED_7Y;
            case "8 år" -> MortgageTerm.FIXED_8Y;
            case "9 år" -> MortgageTerm.FIXED_9Y;
            case "10 år" -> MortgageTerm.FIXED_10Y;
            default -> throw new IllegalArgumentException("Unknown term: " + label);
        };
    }

    /**
     * Översätter enum → mänskligt läsbar label.
     */
    private String toTermLabel(MortgageTerm term) {
        return switch (term) {
            case VARIABLE_3M -> "3 mån";
            case FIXED_1Y -> "1 år";
            case FIXED_2Y -> "2 år";
            case FIXED_3Y -> "3 år";
            case FIXED_4Y -> "4 år";
            case FIXED_5Y -> "5 år";
            case FIXED_6Y -> "6 år";
            case FIXED_7Y -> "7 år";
            case FIXED_8Y -> "8 år";
            case FIXED_9Y -> "9 år";
            case FIXED_10Y -> "10 år";
        };
    }

    /**
     * Formaterar månad till "okt 2025" (svenska).
     */
    private String formatMonth(LocalDate date) {
        if (date == null) return null;

        return date.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("sv"))
                + " " + date.getYear();
    }
}