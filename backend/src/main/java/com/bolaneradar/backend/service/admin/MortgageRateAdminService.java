package com.bolaneradar.backend.service.admin;

import com.bolaneradar.backend.dto.admin.MortgageRateDto;
import com.bolaneradar.backend.dto.mapper.admin.MortgageRateMapper;
import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import com.bolaneradar.backend.service.core.BankService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ================================================================
 * MORTGAGE RATE ADMIN SERVICE
 * ================================================================
 * <p>
 * Ansvar:
 * - Administrativ service för att skapa och spara bolåneräntor
 * - Används av admin-/verktygsflöden, inte av publika endpoints
 * <p>
 * Funktionalitet:
 * - Tar emot MortgageRateDto-objekt
 * - Slår upp tillhörande Bank
 * - Mappar DTO → Entity och sparar i databasen
 * - Returnerar sparade räntor som DTO
 * <p>
 * Designprinciper:
 * - Endast skrivansvar (write-service)
 * - Ingen affärslogik eller beräkning sker här
 * - Mapper ansvarar för konvertering mellan DTO och Entity
 * <p>
 * Säkerhet & drift:
 * - Ska endast användas i kontrollerade miljöer (admin/dev)
 * - Bör inte exponeras direkt utan skydd via controller
 * ================================================================
 */
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

    /**
     * Skapar och sparar en lista med bolåneräntor.
     * Banken måste redan finnas i systemet.
     */
    public List<MortgageRateDto> createRates(List<MortgageRateDto> dtos) {

        return dtos.stream()
                .map(dto -> {
                    Bank bank = bankService.getBankByName(dto.bankName())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Bank inte hittad: " + dto.bankName())
                            );

                    MortgageRate rate = MortgageRateMapper.toEntity(dto, bank);
                    rateRepository.save(rate);

                    return MortgageRateMapper.toDto(rate);
                })
                .toList();
    }
}