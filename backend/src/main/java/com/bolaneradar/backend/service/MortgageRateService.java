package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MortgageRateService {

    private final MortgageRateRepository mortgageRateRepository;

    // Konstruktorinjektion – Spring sköter kopplingen
    public MortgageRateService(MortgageRateRepository mortgageRateRepository) {
        this.mortgageRateRepository = mortgageRateRepository;
    }

    /**
     * Hämta alla bolåneräntor.
     */
    public List<MortgageRate> getAllRates() {
        return mortgageRateRepository.findAll();
    }

    /**
     * Hämta alla räntor kopplade till en specifik bank.
     */
    public List<MortgageRate> getRatesByBank(Bank bank) {
        return mortgageRateRepository.findByBank(bank);
    }

    /**
     * Spara en ny bolåneränta i databasen.
     * Om banken redan finns kopplas räntan dit.
     */
    public MortgageRate saveRate(MortgageRate rate) {
        return mortgageRateRepository.save(rate);
    }

    /**
     * Ta bort en ränta baserat på ID (för framtida användning).
     */
    public void deleteRate(Long id) {
        mortgageRateRepository.deleteById(id);
    }
}
