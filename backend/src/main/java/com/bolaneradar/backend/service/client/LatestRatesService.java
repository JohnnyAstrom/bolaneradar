package com.bolaneradar.backend.service.client;

import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LatestRatesService {

    private final MortgageRateRepository rateRepository;

    public LatestRatesService(MortgageRateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    public List<MortgageRate> getLatestRatesByType(RateType rateType) {
        return rateRepository.findLatestRatesByType(rateType)
                .stream()
                .sorted(Comparator
                        .comparing((MortgageRate r) -> r.getBank().getName())
                        .thenComparing(r -> r.getTerm().ordinal())
                )
                .toList();
    }
}