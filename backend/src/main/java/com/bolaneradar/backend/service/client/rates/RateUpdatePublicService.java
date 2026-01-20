package com.bolaneradar.backend.service.client.rates;

import com.bolaneradar.backend.dto.api.RateUpdateDayDto;
import com.bolaneradar.backend.dto.api.RateUpdateDto;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


/**
 * Client-service som ansvarar för att bygga listan
 * "Senaste bankuppdateringar".
 * <p>
 * Ansvar:
 * - Identifiera faktiska ändringar i bankernas listräntor
 * - Jämföra historiska räntor per bank och bindningstid
 * - Gruppera ändringar per datum för presentation i frontend
 * <p>
 * Datakälla:
 * - MortgageRate (endast LISTRATE)
 * <p>
 * Prestanda:
 * - Ett enda databasanrop
 * - All jämförelse- och grupplogik sker i minnet
 * - Historiken begränsas i tid för rimlig svarstid
 * <p>
 * Sortering:
 * - Datum: senaste först
 * - Inom varje datum:
 * - Bank (alfabetiskt)
 * - Bindningstid (3 mån → 10 år enligt enum-ordning)
 * <p>
 * Exponeras via:
 * - RateUpdatePublicController
 */
@Service
public class RateUpdatePublicService {

    private final MortgageRateRepository mortgageRateRepository;

    public RateUpdatePublicService(MortgageRateRepository mortgageRateRepository) {
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Hämtar alla faktiska ändringar av listräntor,
     * grupperade per datum (senaste först).
     */
    public List<RateUpdateDayDto> getRateUpdates() {

        // 1. Begränsa hur långt bak vi tittar (justerbart)
        LocalDate fromDate = LocalDate.now().minusMonths(12);

        // 2. Hämta alla relevanta listräntor i korrekt sortering
        //    (bank → term → effectiveDate desc)
        List<MortgageRate> rates =
                mortgageRateRepository.findAllListRatesSortedFrom(fromDate);

        // 3. Temporär struktur: datum -> ändringar
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

        // 5. Sortera ändringar per datum:
        //    - Bank (A–Ö)
        //    - Bindningstid (enum-ordning: 3 mån → 10 år)
        for (List<RateUpdateDto> updates : updatesByDate.values()) {
            updates.sort(
                    Comparator
                            .comparing(RateUpdateDto::getBankName)
                            .thenComparing(dto ->
                                    MortgageTerm.valueOf(dto.getBindingPeriod()).ordinal()
                            )
            );
        }

        // 6. Bygg slutlig DTO-lista, sorterad på datum (senaste först)
        return updatesByDate.entrySet()
                .stream()
                .sorted(Map.Entry.<LocalDate, List<RateUpdateDto>>comparingByKey().reversed())
                .map(entry -> new RateUpdateDayDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}