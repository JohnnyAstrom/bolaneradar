package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.RateUpdateDayDto;
import com.bolaneradar.backend.dto.api.RateUpdateDto;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RateUpdatesService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository mortgageRateRepository;

    public RateUpdatesService(
            BankRepository bankRepository,
            MortgageRateRepository mortgageRateRepository
    ) {
        this.bankRepository = bankRepository;
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Hämtar alla faktiska ändringar av listräntor,
     * grupperade per datum (senaste först).
     */
    public List<RateUpdateDayDto> getRateUpdates() {

        // 1. Temporär struktur: datum -> ändringar
        Map<LocalDate, List<RateUpdateDto>> updatesByDate = new HashMap<>();

        // 2. Hämta alla banker
        List<Bank> banks = bankRepository.findAll();

        for (Bank bank : banks) {

            // 3. För varje bindningstid
            for (MortgageTerm term : MortgageTerm.values()) {

                List<MortgageRate> history =
                        mortgageRateRepository.findByBankAndTermAndRateTypeOrderByEffectiveDateDesc(
                                bank,
                                term,
                                RateType.LISTRATE
                        );

                if (history.size() < 2) {
                    continue;
                }

                // 4. Jämför sekventiellt
                for (int i = 0; i < history.size() - 1; i++) {

                    MortgageRate current = history.get(i);
                    MortgageRate previous = history.get(i + 1);

                    if (current.getRatePercent().compareTo(previous.getRatePercent()) != 0) {

                        LocalDate changeDate = current.getEffectiveDate();

                        RateUpdateDto dto = new RateUpdateDto(
                                bank.getName(),
                                bank.getName(),
                                term.name(),
                                previous.getRatePercent(),
                                current.getRatePercent()
                        );

                        updatesByDate
                                .computeIfAbsent(changeDate, d -> new ArrayList<>())
                                .add(dto);
                    }
                }
            }
        }

        // 5. Sortera datum: senaste först
        List<LocalDate> sortedDates = updatesByDate.keySet()
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        // 6. Bygg slutlig DTO-lista
        List<RateUpdateDayDto> result = new ArrayList<>();

        for (LocalDate date : sortedDates) {
            result.add(
                    new RateUpdateDayDto(
                            date,
                            updatesByDate.get(date)
                    )
            );
        }

        return result;
    }
}