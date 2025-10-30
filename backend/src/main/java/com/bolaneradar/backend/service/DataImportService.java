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

        });

        // enklare: skapa deleteAll() i RateUpdateLogRepository
        rateUpdateService.deleteAllLogs();

        // ta bort alla räntor
        rateRepository.deleteAll();

        // Skapa (eller hämta) banker utan att ta bort gamla
        Bank swedbank = bankRepository.findByName("Swedbank");
        if (swedbank == null) {
            swedbank = new Bank("Swedbank", "https://www.swedbank.se");
            bankRepository.save(swedbank);
        }

        Bank nordea = bankRepository.findByName("Nordea");
        if (nordea == null) {
            nordea = new Bank("Nordea", "https://www.nordea.se");
            bankRepository.save(nordea);
        }

        Bank handelsbanken = bankRepository.findByName("Handelsbanken");
        if (handelsbanken == null) {
            handelsbanken = new Bank("Handelsbanken", "https://www.handelsbanken.se");
            bankRepository.save(handelsbanken);
        }

        Bank seb = bankRepository.findByName("SEB");
        if (seb == null) {
            seb = new Bank("SEB", "https://seb.se");
            bankRepository.save(seb);
        }

        Bank sbab = bankRepository.findByName("SBAB");
        if (sbab == null) {
            sbab = new Bank("SBAB", "https://www.sbab.se");
            bankRepository.save(sbab);
        }


//        List<MortgageRate> rates = List.of(
//                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("3.75"), LocalDate.now()),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now()),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.50"), LocalDate.now()),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.30"), LocalDate.now()),
//                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(1)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(1)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.60"), LocalDate.now().minusDays(1)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.40"), LocalDate.now().minusDays(1)),
//                new MortgageRate(swedbank, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.05"), LocalDate.now().minusDays(2)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now().minusDays(2)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.60"), LocalDate.now().minusDays(2)),
//                new MortgageRate(swedbank, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.40"), LocalDate.now().minusDays(2)),
//
//                new MortgageRate(nordea, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("3.85"), LocalDate.now()),
//                new MortgageRate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("3.90"), LocalDate.now()),
//                new MortgageRate(nordea, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.70"), LocalDate.now()),
//                new MortgageRate(nordea, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.75"), LocalDate.now()),
//                new MortgageRate(nordea, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.00"), LocalDate.now().minusDays(1)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("4.00"), LocalDate.now().minusDays(1)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now().minusDays(1)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.65"), LocalDate.now().minusDays(1)),
//                new MortgageRate(nordea, MortgageTerm.VARIABLE_3M, RateType.LISTRATE, new BigDecimal("4.20"), LocalDate.now().minusDays(2)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_1Y, RateType.LISTRATE, new BigDecimal("4.10"), LocalDate.now().minusDays(2)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_3Y, RateType.LISTRATE, new BigDecimal("3.80"), LocalDate.now().minusDays(2)),
//                new MortgageRate(nordea, MortgageTerm.FIXED_5Y, RateType.LISTRATE, new BigDecimal("3.65"), LocalDate.now().minusDays(2))
//        );
//
//        rateRepository.saveAll(rates);
//
//        // Logga importen
//        rateUpdateService.logUpdate(swedbank, "ExampleData", rates.size());
    }
}
