package com.bolaneradar.backend.service.integration.scraper.api;

import com.bolaneradar.backend.entity.Bank;
import com.bolaneradar.backend.entity.MortgageRate;
import java.io.IOException;
import java.util.List;

/**
 * Gränssnitt som alla bank-scrapers implementerar.
 * Varje scraper ansvarar för att hämta bolåneräntor från en specifik banks webbplats.
 */
public interface BankScraper {

    /**
     * Hämtar bolåneräntor för den angivna banken via webbskrapning.
     * @param bank banken vars räntor ska hämtas
     * @return en lista av MortgageRate-objekt som representerar bankens aktuella räntor
     * @throws IOException om något går fel vid anslutning eller hämtning av data
     */
    List<MortgageRate> scrapeRates(Bank bank) throws IOException;
}
