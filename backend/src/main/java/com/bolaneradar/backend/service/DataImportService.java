package com.bolaneradar.backend.service;

import com.bolaneradar.backend.model.Bank;
import com.bolaneradar.backend.model.MortgageRate;
import com.bolaneradar.backend.model.MortgageTerm;
import com.bolaneradar.backend.model.RateType;
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
    private final RateUpdateService rateUpdateService;

    public DataImportService(BankRepository bankRepository,
                             MortgageRateRepository rateRepository,
                             RateUpdateService rateUpdateService) {
        this.bankRepository = bankRepository;
        this.rateRepository = rateRepository;
        this.rateUpdateService = rateUpdateService;
    }


    /**
     * Skapar exempeldata för att testa systemet.
     */
    public void importExampleData() {

        // Ta bort loggar först (de har foreign key till bank)
        rateUpdateService.getAllLogs().forEach(log -> {
            // du kan skapa en deleteAll-metod i RateUpdateService istället
        });

        // enklare: skapa deleteAll() i RateUpdateLogRepository
        rateUpdateService.deleteAllLogs(); // (se steg nedan)

        // Rensa banker
        bankRepository.deleteAll();

        // Skapa ny bank och räntor
        Bank swedbank = new Bank("Swedbank", "https://www.swedbank.se");
        bankRepository.save(swedbank);

        List<MortgageRate> rates = List.of(
                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.25"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("4.10"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.95"), LocalDate.now()),
                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now())
        );

        rateRepository.saveAll(rates);

        // Logga importen
        rateUpdateService.logUpdate(swedbank, "ExampleData", rates.size());
    }
}
