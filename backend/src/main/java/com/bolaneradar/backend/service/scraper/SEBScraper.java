package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Webbskrapare för SEB.
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor) bolåneräntor.
 * <p>
 * Sidan visar räntorna i två inbäddade iframes (en för listräntor och en för snitträntor),
 * vilket kräver Selenium för att öppna huvud­sidan, hämta iframe-URL:erna och sedan ladda varje iframe separat.
 * <ul>
 *   <li>Startar Chrome i headless-läge</li>
 *   <li>Identifierar iframe-länkar med "mortgage" i src</li>
 *   <li>Öppnar varje iframe separat och hämtar tabellrader med räntor</li>
 *   <li>Returnerar alla räntor som MortgageRate-objekt</li>
 * </ul>
 */
@Service
public class SEBScraper implements BankScraper {

    private static final String BASE_URL = "https://seb.se/privat/bolan/bolanerantor";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        System.out.println("Startar skrapning för SEB...");
        List<MortgageRate> rates = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get(BASE_URL);

            // Vänta tills minst en iframe finns
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("iframe")));
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

            // Filtrera ut relevanta iframe-URL:er
            List<String> iframeUrls = iframes.stream()
                    .map(i -> i.getDomAttribute("src"))
                    .filter(src -> src != null && src.contains("mortgage"))
                    .toList();

            driver.quit(); // stäng huvuddrivern

            // Iterera över iframes
            for (String url : iframeUrls) {
                RateType rateType = url.contains("average") ? RateType.AVERAGERATE : RateType.LISTRATE;
                System.out.println("[SEB] Bearbetar iframe: " + url + " (" + rateType + ")");
                rates.addAll(scrapeIframe(bank, url, rateType, options));
            }

            System.out.println("SEB: totalt " + rates.size() + " räntor hittade.");

        } catch (Exception e) {
            System.err.println("Fel vid SEB-scraping: " + e.getMessage());
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("seb_debug.html"), driver.getPageSource());
                System.out.println("Skrev ut sidans HTML till seb_debug.html");
            } catch (Exception ignored) {}
        } finally {
            try { driver.quit(); } catch (Exception ignored) {}
        }

        return rates;
    }

    /**
     * Läser en iframe och returnerar extraherade räntor.
     * Hanterar både listräntor och snitträntor.
     */
    private List<MortgageRate> scrapeIframe(Bank bank, String url, RateType rateType, ChromeOptions options) {
        List<MortgageRate> list = new ArrayList<>();
        WebDriver iframeDriver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(iframeDriver, Duration.ofSeconds(8));

        try {
            iframeDriver.get(url);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));

            List<WebElement> rows = iframeDriver.findElements(By.cssSelector("table tbody tr"));
            for (WebElement row : rows) {
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.isEmpty()) continue;

                String termText = cols.get(0).getText().toLowerCase().trim();
                String rateText = cols.size() > 1 ? cols.get(1).getText().replace("%", "").replace(",", ".").trim() : null;
                String monthText = (rateType == RateType.AVERAGERATE && cols.size() > 2)
                        ? cols.get(2).getText().trim()
                        : null;

                MortgageTerm term = ScraperUtils.parseTerm(termText);
                BigDecimal rate = ScraperUtils.parseRate(rateText);
                LocalDate date = LocalDate.now();

                // 🗓 Om det är snittränta (AVERAGERATE), försök tolka “Avser månad”
                if (rateType == RateType.AVERAGERATE && monthText != null && !monthText.isEmpty()) {
                    date = parseMonthYear(monthText);
                }

                if (term != null && rate != null) {
                    list.add(new MortgageRate(bank, term, rateType, rate, date));
                    System.out.println(rateType + " | " + termText + " = " + rate + "% (" + date + ")");
                }
            }

        } catch (Exception e) {
            System.err.println("Fel vid läsning av SEB-iframe (" + rateType + "): " + e.getMessage());
        } finally {
            iframeDriver.quit();
        }

        return list;
    }

    /**
     * Konverterar text som "September 2025" till LocalDate (första dagen i månaden).
     */
    private LocalDate parseMonthYear(String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length == 2) {
                String monthName = parts[0].toLowerCase();
                int year = Integer.parseInt(parts[1]);
                int month = switch (monthName) {
                    case "januari", "january" -> 1;
                    case "februari", "february" -> 2;
                    case "mars", "march" -> 3;
                    case "april" -> 4;
                    case "maj", "may" -> 5;
                    case "juni", "june" -> 6;
                    case "juli", "july" -> 7;
                    case "augusti", "august" -> 8;
                    case "september" -> 9;
                    case "oktober", "october" -> 10;
                    case "november" -> 11;
                    case "december" -> 12;
                    default -> 1;
                };
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            System.err.println("Kunde inte tolka månad/år: " + text);
        }
        return LocalDate.now();
    }
}