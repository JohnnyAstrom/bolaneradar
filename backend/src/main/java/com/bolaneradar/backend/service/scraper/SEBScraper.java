package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Webbskrapare för SEB.
 * Öppnar direkt de iframes som innehåller ränteinformationen istället för att växla till dem.
 */
@Service
public class SEBScraper implements BankScraper {

    private static final String BASE_URL = "https://seb.se/privat/bolan/bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        List<MortgageRate> rates = new ArrayList<>();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            System.out.println("🔎 SEB: öppnar sida...");
            driver.get(BASE_URL);
            Thread.sleep(3000); // Vänta på att iframes laddas

            // Hämta iframes och deras src-länkar
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            List<String> iframeUrls = new ArrayList<>();

            for (WebElement iframe : iframes) {
                try {
                    String src = iframe.getAttribute("src");
                    if (src != null && src.contains("mortgage")) {
                        iframeUrls.add(src);
                    }
                } catch (Exception ignored) {}
            }

            System.out.println("🔗 Hittade iframe-URL:er:");
            iframeUrls.forEach(url -> System.out.println("   " + url));

            driver.quit(); // Stäng första drivrutinen (vi öppnar nya för varje iframe)

            for (String iframeUrl : iframeUrls) {
                RateType rateType = iframeUrl.contains("average") ? RateType.AVERAGERATE : RateType.LISTRATE;
                System.out.println("➡️  Bearbetar iframe: " + iframeUrl + " (" + rateType + ")");

                // Ny headless session för varje iframe (mer stabilt)
                ChromeDriver iframeDriver = new ChromeDriver(options);
                iframeDriver.get(iframeUrl);

                WebDriverWait wait = new WebDriverWait(iframeDriver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody tr")));

                List<WebElement> rows = iframeDriver.findElements(By.cssSelector("table tbody tr"));
                System.out.println("   Hittade " + rows.size() + " rader i " + rateType + "-tabell.");

                for (WebElement row : rows) {
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() >= 2) {
                        String termText = cols.get(0).getText().toLowerCase();
                        String rateText = cols.get(1).getText()
                                .replace("%", "")
                                .replace(",", ".")
                                .trim();

                        MortgageTerm term = ScraperUtils.parseTerm(termText);
                        if (term != null && !rateText.isEmpty()) {
                            try {
                                BigDecimal rate = new BigDecimal(rateText);
                                rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                iframeDriver.quit();
            }

            System.out.println("✅ SEB: hittade totalt " + rates.size() + " räntor.");

        } catch (Exception e) {
            System.err.println("⚠️ Fel vid SEB-scraping: " + e.getMessage());
        } finally {
            try {
                driver.quit();
            } catch (Exception ignored) {}
        }

        return rates;
    }
}