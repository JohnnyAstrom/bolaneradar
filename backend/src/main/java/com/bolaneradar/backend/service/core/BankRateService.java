package com.bolaneradar.backend.service.core;

import com.bolaneradar.backend.dto.core.BankRateRowDto;
import com.bolaneradar.backend.dto.core.BankRateHistoryDto; // <-- lägg till
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

    public Map<String, Object> getRatesForBank(String bankName) {

        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        LocalDate latestAvgDate = rateRepository.findLatestAverageDateForBank(bank.getId());
        LocalDate latestMonth = latestAvgDate != null ? latestAvgDate.withDayOfMonth(1) : null;

        Map<MortgageTerm, List<MortgageRate>> grouped =
                bank.getMortgageRates().stream()
                        .collect(Collectors.groupingBy(MortgageRate::getTerm));

        List<BankRateRowDto> rows = new ArrayList<>();

        for (var entry : grouped.entrySet()) {

            MortgageTerm term = entry.getKey();
            String termLabel = toTermLabel(term);

            MortgageRate latestList =
                    rateRepository.findFirstByBankIdAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank.getId(), term, RateType.LISTRATE
                    );

            MortgageRate latestAvg = null;

            if (latestMonth != null) {
                latestAvg = rateRepository.findAverageRateForBankAndTermAndMonth(
                        bank.getId(),
                        term,
                        latestMonth,
                        latestMonth.plusMonths(1)
                );
            }

            rows.add(BankRateMapper.toDto(termLabel, latestList, latestAvg));
        }

        rows.sort(Comparator.comparing(
                row -> SORT_ORDER.indexOf(mapLabelToTerm(row.term()))
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("month", latestMonth);
        result.put("monthFormatted", formatMonth(latestMonth));
        result.put("rows", rows);

        return result;
    }


    // ========================================================================
    // =======================   HJÄLPFUNKTIONER   ============================
    // ========================================================================

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

    private String formatMonth(LocalDate date) {
        if (date == null) return null;

        return date.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("sv"))
                + " " + date.getYear();
    }


    // ========================================================================
    // =====================   NY FUNKTION: HISTORIK   ========================
    // ========================================================================

    /**
     * Returnerar snitträntor per månad för senaste 12 månaderna.
     */
    public List<BankRateHistoryDto> getHistoricalAverageRates(String bankName, MortgageTerm term) {

        // Hitta bank
        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        // Hämta all historik för term + AVERAGERATE (sorterad DESC)
        List<MortgageRate> allRates =
                rateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                        bank, term, RateType.AVERAGERATE
                );

        LocalDate start = LocalDate.now()
                .minusMonths(12)
                .withDayOfMonth(1);

        // Filtrera senaste 12 månaderna
        List<MortgageRate> last12 =
                allRates.stream()
                        .filter(r -> !r.getEffectiveDate().isBefore(start))
                        .toList();

        // Konvertera till DTO-lista
        return BankRateMapper.toHistoryDto(last12);
    }

    // ========================================================================
// ===========   HISTORIK: TILLGÄNGLIGA BINDNINGSTIDER   ===================
// ========================================================================

    /**
     * Returnerar alla bindningstider som har tillräckligt med historisk data
     * (minst 10 datapunkter under senaste 12 månaderna).
     */
    public List<MortgageTerm> getAvailableTerms(String bankName) {

        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        List<MortgageTerm> available = new ArrayList<>();

        LocalDate start = LocalDate.now()
                .minusMonths(12)
                .withDayOfMonth(1);

        for (MortgageTerm term : MortgageTerm.values()) {

            // Hämta all historisk snittränta för term
            List<MortgageRate> allRates =
                    rateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank,
                            term,
                            RateType.AVERAGERATE
                    );

            // Räkna datapunkter senaste 12 mån
            long count = allRates.stream()
                    .filter(r -> !r.getEffectiveDate().isBefore(start))
                    .count();

            // Visa om det saknas max 2 månader (dvs minst 10 datapunkter)
            if (count >= 10) {
                available.add(term);
            }
        }

        return available;
    }
}