package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
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
 * Webbskrapare för Landshypotek Bank.
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor) bolåneräntor.
 * <p>
 * Räntorna presenteras i två separata sektioner (accordion-komponenter) där innehållet
 * renderas dynamiskt av JavaScript först efter att sektionen expanderats.
 * Selenium används för att:
 * <ul>
 *   <li>Öppna sidan i headless-läge.</li>
 *   <li>Hantera cookie-bannern (Cookiebot) via klick på kända knappar, eller ta bort bannern med JavaScript om den blockerar interaktion.</li>
 *   <li>Klicka upp sektionerna "Listräntor för bolån" och "Snitträntor för bolån senaste månaden".</li>
 *   <li>Extrahera tabellvärden dynamiskt när sektionen är synlig i DOM.</li>
 * </ul>
 * Optimerad för kortare körningstid (~10–15 sekunder).
 */
@Service
public class LandshypotekBankScraper implements BankScraper {

    private static final String URL = "https://www.landshypotek.se/lana/bolanerantor/";

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        System.out.println("Startar skrapning för Landshypotek Bank...");
        List<MortgageRate> rates = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

            // === 1️⃣ Hantera cookie-banner (Cookiebot) ===
            try {
                List<By> cookieButtons = List.of(
                        By.id("CybotCookiebotDialogBodyButtonAccept"),
                        By.id("CybotCookiebotDialogBodyButtonAllowAll"),
                        By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll"),
                        By.id("CybotCookiebotDialogBodyLevelButtonAccept"),
                        By.id("CybotCookiebotDialogBodyButtonDecline"),
                        By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'godkänn')]"),
                        By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'tillåt')]"),
                        By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acceptera')]")
                );

                boolean cookieClosed = false;
                for (By selector : cookieButtons) {
                    try {
                        WebElement btn = shortWait.until(ExpectedConditions.presenceOfElementLocated(selector));
                        if (btn.isDisplayed()) {
                            btn.click();
                            System.out.println("Cookie-banner stängd via selector: " + selector);
                            cookieClosed = true;
                            break;
                        }
                    } catch (Exception ignored) {}
                }

                if (!cookieClosed) {
                    ((JavascriptExecutor) driver).executeScript("""
                        var el = document.getElementById('CybotCookiebotDialog');
                        if (el) el.remove();
                    """);
                    System.out.println("Cookie-banner borttagen med JavaScript.");
                }
            } catch (Exception e) {
                System.out.println("Ingen cookie-banner att stänga.");
            }

            // === 2️⃣ Klicka upp "Listräntor för bolån" ===
            WebElement listRateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Listräntor för bolån')]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", listRateButton);
            listRateButton.click();

            WebElement listRateTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[contains(.,'Listräntor för bolån')]/following::table[1]")
            ));
            extractRatesFromTable(bank, listRateTable, RateType.LISTRATE, rates);
            System.out.println("Hämtade listräntor.");

            // === 3️⃣ Klicka upp "Snitträntor för bolån senaste månaden" ===
            WebElement avgRateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Snitträntor')]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", avgRateButton);
            avgRateButton.click();

            WebElement avgRateTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[contains(.,'Snitträntor')]/following::table[1]")
            ));
            extractRatesFromTable(bank, avgRateTable, RateType.AVERAGERATE, rates);
            System.out.println("Hämtade snitträntor.");

            System.out.println("Landshypotek Bank: totalt " + rates.size() + " räntor hittade.");

        } catch (Exception e) {
            System.err.println("Fel vid skrapning av Landshypotek Bank: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return rates;
    }

    private void extractRatesFromTable(Bank bank, WebElement table, RateType rateType, List<MortgageRate> rates) {
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        for (int i = 1; i < rows.size(); i++) { // hoppa över rubrikraden
            List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
            if (cols.size() < 2) continue;

            String termText = cols.get(0).getText().toLowerCase().trim();
            String rateText = cols.get(1).getText()
                    .replace("%", "")
                    .replace(",", ".")
                    .trim();

            MortgageTerm term = ScraperUtils.parseTerm(termText);
            BigDecimal rate = ScraperUtils.parseRate(rateText);

            if (term != null && rate != null) {
                rates.add(new MortgageRate(bank, term, rateType, rate, LocalDate.now()));
                System.out.println(rateType + " | " + termText + " = " + rate + "%");
            }
        }
    }
}
