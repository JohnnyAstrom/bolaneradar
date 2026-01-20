package com.bolaneradar.backend.service.client.banks;

import com.bolaneradar.backend.dto.api.BankRateHistoryDto;
import com.bolaneradar.backend.dto.mapper.api.BankRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * BANK HISTORY SERVICE
 * ================================================================
 * <p>
 * Ansvar:
 * - Tillhandahåller historisk snitträntedata per bank
 * - Används av bankens graf- och historikvy
 * <p>
 * Användning:
 * - Anropas av bank-history-controller
 * - Stöder val av bindningstid (MortgageTerm)
 * <p>
 * Funktionalitet:
 * - Hämtar snitträntor (AVERAGERATE) för vald bank och bindningstid
 * - Begränsar historiken till de senaste 12 månaderna
 * - Returnerar data i graf-vänligt DTO-format
 * <p>
 * Kompletterande logik:
 * - Identifierar vilka bindningstider som har tillräcklig historik
 * - En term anses giltig om den har minst ~10 datapunkter senaste året
 * <p>
 * Designprinciper:
 * - Service-lagret innehåller all filtrering och affärslogik
 * - Repository används endast för rå datainhämtning
 * - Mapper ansvarar för DTO-konvertering
 * <p>
 * Prestanda:
 * - Begränsad historik (12 månader)
 * - Små datamängder per bank och term
 * - Optimerad för grafrendering i frontend
 * ================================================================
 */
@Service
public class BankHistoryService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;

    public BankHistoryService(
            BankRepository bankRepository,
            MortgageRateRepository rateRepository
    ) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
    }

    // -------------------------------------------------------------
    // HISTORIK (senaste 12 månaderna)
    // -------------------------------------------------------------
    public List<BankRateHistoryDto> getHistoricalAverageRates(
            String bankName,
            MortgageTerm term
    ) {
        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        List<MortgageRate> allRates =
                rateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                        bank, term, RateType.AVERAGERATE
                );

        LocalDate start = LocalDate.now().minusMonths(12).withDayOfMonth(1);

        List<MortgageRate> last12 =
                allRates.stream().filter(r -> !r.getEffectiveDate().isBefore(start)).toList();

        return BankRateMapper.toHistoryDto(last12);
    }

    // -------------------------------------------------------------
    // TILLGÄNGLIGA TERMER
    // -------------------------------------------------------------
    public List<MortgageTerm> getAvailableTerms(String bankName) {

        Bank bank = bankRepository.findByNameIgnoreCase(bankName)
                .orElseThrow(() -> new IllegalArgumentException("Bank not found: " + bankName));

        LocalDate start = LocalDate.now().minusMonths(12).withDayOfMonth(1);

        List<MortgageTerm> available = new ArrayList<>();

        for (MortgageTerm term : MortgageTerm.values()) {

            List<MortgageRate> allRates =
                    rateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                            bank,
                            term,
                            RateType.AVERAGERATE
                    );

            long countLast12 = allRates.stream()
                    .filter(r -> !r.getEffectiveDate().isBefore(start))
                    .count();

            if (countLast12 >= 10) {
                available.add(term);
            }
        }

        return available;
    }
}