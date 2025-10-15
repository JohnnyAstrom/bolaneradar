package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.repository.BankRepository;
import com.bolaneradar.backend.repository.MortgageRateRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * En enkel tjänst som kan skapa exempeldata i databasen.
 * På sikt kommer denna hämta riktiga data från bankernas sidor.
 */
@Service
public class DataImportService {

    private final BankRepository bankRepository;
    private final MortgageRateRepository rateRepository;

    public DataImportService(BankRepository bankRepository, MortgageRateRepository rateRepository) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
    }


    /**
     * Skapar exempeldata för att testa systemet.
     */
    public void importExampleData() {

        bankRepository.deleteAll();

        Bank swedbank = new Bank("Swedbank", "https://www.swedbank.se");
        bankRepository.save(swedbank);

        List<MortgageRate> rates = List.of(
                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, new BigDecimal("4.25"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, new BigDecimal("4.10"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, new BigDecimal("3.95"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, new BigDecimal("3.80"), LocalDate.now())
        );

        rateRepository.saveAll(rates);
    }
}
