package com.bolaneradar.backend.service.integration.scraper.bank;

import com.bolaneradar.backend.entity.core.Bank;
import com.bolaneradar.backend.entity.core.MortgageRate;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
import com.bolaneradar.backend.service.integration.scraper.api.BankScraper;
import com.bolaneradar.backend.service.integration.scraper.support.ScraperUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Webbskrapare för SEB.
 * Seiten visar räntor i två iframes (list/snitts), därför används Selenium.
 */
@Service
public class SEBScraper implements BankScraper {

    private static final String BASE_URL = "https://seb.se/privat/bolan/bolanerantor";

    @Override
    public String getBankName() {
        return "SEB";
    }

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        List<MortgageRate> rates = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        try {
            driver.get(BASE_URL);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("iframe")));
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

            List<String> iframeUrls = iframes.stream()
                    .map(i -> i.getDomAttribute("src"))
                    .filter(src -> src != null && src.contains("mortgage"))
                    .toList();

            driver.quit(); // stäng huvuddrivern – vi öppnar varje iframe separat

            for (String url : iframeUrls) {
                RateType rateType = url.toLowerCase().contains("average") ? RateType.AVERAGERATE : RateType.LISTRATE;
                rates.addAll(scrapeIframe(bank, url, rateType, options));
            }

        } catch (Exception e) {
            System.err.println("SEB-scraping fel: " + e.getMessage());
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("seb_debug.html"), driver.getPageSource());
            } catch (Exception ignored) {}
        } finally {
            try { driver.quit(); } catch (Exception ignored) {}
        }

        ScraperUtils.logResult("SEB", rates.size());
        return rates;
    }

    /**
     * Läser en iframe och returnerar extraherade räntor (list/snitts).
     */
    private List<MortgageRate> scrapeIframe(Bank bank, String url, RateType rateType, ChromeOptions options) {
        List<MortgageRate> list = new ArrayList<>();

        WebDriver d = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(d, Duration.ofSeconds(10));

        try {
            d.get(url);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr, table tr")));

            List<WebElement> rows = d.findElements(By.cssSelector("table tbody tr"));
            if (rows.isEmpty()) rows = d.findElements(By.cssSelector("table tr"));

            // Försök läsa ut månad för snittränta från tabell eller omgivande text
            LocalDate avgMonthDate = null;
            if (rateType == RateType.AVERAGERATE) {
                String pageText = d.getPageSource().replaceAll("<[^>]*>", " ");
                YearMonth ym = ScraperUtils.parseSwedishMonth(pageText);
                avgMonthDate = ym.atDay(1);
            }

            for (WebElement row : rows) {
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() < 2) continue;

                String termText = cols.get(0).getText();
                String rateText = cols.get(1).getText();

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);
                if (term == null || rate == null) continue;

                LocalDate date = (rateType == RateType.AVERAGERATE && avgMonthDate != null)
                        ? avgMonthDate
                        : LocalDate.now();

                // Om snitt-tabellen har en kolumn med “Avser månad”, använd den i första hand
                if (rateType == RateType.AVERAGERATE && cols.size() > 2) {
                    String monthText = cols.get(2).getText();
                    if (monthText != null && !monthText.isBlank()) {
                        YearMonth ym = ScraperUtils.parseSwedishMonth(monthText);
                        date = ym.atDay(1);
                    }
                }

                list.add(new MortgageRate(bank, term, rateType, rate, date));
            }

        } catch (Exception e) {
            System.err.println("SEB iframe-fel (" + rateType + "): " + e.getMessage());
        } finally {
            d.quit();
        }

        return list;
    }
}