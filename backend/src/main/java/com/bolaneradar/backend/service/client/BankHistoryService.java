package com.bolaneradar.backend.service.client;

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