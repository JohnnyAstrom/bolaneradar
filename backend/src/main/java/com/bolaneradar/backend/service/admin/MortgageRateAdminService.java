package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.dto.admin.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.admin.MortgageRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MortgageRateAdminService {

    private final MortgageRateRepository rateRepository;
    private final BankService bankService;

    public MortgageRateAdminService(
            MortgageRateRepository rateRepository,
            BankService bankService
    ) {
        this.rateRepository = rateRepository;
        this.bankService = bankService;
    }

    public List<MortgageRateDto> createRates(List<MortgageRateDto> dtos) {

        return dtos.stream()
                .map(dto -> {
                    Bank bank = bankService.getBankByName(dto.bankName())
                            .orElseThrow(() -> new IllegalArgumentException("Bank inte hittad: " + dto.bankName()));

                    MortgageRate rate = MortgageRateMapper.toEntity(dto, bank);
                    rateRepository.save(rate);

                    return MortgageRateMapper.toDto(rate);
                })
                .toList();
    }
}