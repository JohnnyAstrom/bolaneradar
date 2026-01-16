package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.dto.api.RateUpdateDayDto;
import com.bolaneradar.backend.dto.api.RateUpdateDto;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RateUpdatesService {

    private final MortgageRateRepository mortgageRateRepository;

    public RateUpdatesService(MortgageRateRepository mortgageRateRepository) {
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Hämtar alla faktiska ändringar av listräntor,
     * grupperade per datum (senaste först).
     *
     * Prestanda:
     * - Ett enda DB-anrop
     * - All jämförelselogik sker i minnet
     * - Historiken tidsbegränsas för rimlig svarstid
     */
    public List<RateUpdateDayDto> getRateUpdates() {

        // 1. Begränsa hur långt bak vi tittar (justerbart)
        LocalDate fromDate = LocalDate.now().minusMonths(12);

        // 2. Hämta alla relevanta listräntor i rätt sortering
        List<MortgageRate> rates =
                mortgageRateRepository.findAllListRatesSortedFrom(fromDate);

        // 3. Datum -> ändringar
        Map<LocalDate, List<RateUpdateDto>> updatesByDate = new HashMap<>();

        // 4. Jämför sekventiellt (listan är redan sorterad)
        for (int i = 0; i < rates.size() - 1; i++) {

            MortgageRate current = rates.get(i);
            MortgageRate previous = rates.get(i + 1);

            // Säkerställ att vi jämför samma bank + bindningstid
            if (!current.getBank().getId().equals(previous.getBank().getId())) {
                continue;
            }
            if (current.getTerm() != previous.getTerm()) {
                continue;
            }

            // Om räntan ändrats
            if (current.getRatePercent().compareTo(previous.getRatePercent()) != 0) {

                LocalDate changeDate = current.getEffectiveDate();

                RateUpdateDto dto = new RateUpdateDto(
                        current.getBank().getName(),
                        current.getTerm().name(),
                        previous.getRatePercent(),
                        current.getRatePercent()
                );

                updatesByDate
                        .computeIfAbsent(changeDate, d -> new ArrayList<>())
                        .add(dto);
            }
        }

        // 5. Sortera datum: senaste först
        return updatesByDate.entrySet()
                .stream()
                .sorted(Map.Entry.<LocalDate, List<RateUpdateDto>>comparingByKey().reversed())
                .map(entry -> new RateUpdateDayDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}