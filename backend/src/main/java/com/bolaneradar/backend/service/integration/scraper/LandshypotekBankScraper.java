package com.bolaneradar.backend.service.integration.scraper;

import com.bolaneradar.backend.entity.*;
import com.bolaneradar.backend.entity.enums.MortgageTerm;
import com.bolaneradar.backend.entity.enums.RateType;
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
 * Webbskrapare för Landshypotek Bank.
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor).
 * <p>
 * Selenium används eftersom sidan laddas dynamiskt.
 * Parseringshjälp sker via ScraperUtils.
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

        try {
            driver.get(URL);
            handleCookieBanner(driver, shortWait);

            // === Listräntor === (oförändrad)
            WebElement listRateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Listräntor för bolån')]")));
            scrollAndClick(driver, listRateButton);

            WebElement listRateTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[contains(.,'Listräntor för bolån')]/following::table[1]")));

            extractRatesFromTable(bank, listRateTable, RateType.LISTRATE, rates, LocalDate.now());
            System.out.println("Hämtade listräntor.");

            // === Snitträntor (från "Historisk snittränta för bolån") ===
            try {
                WebElement historyButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[.//span[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ','abcdefghijklmnopqrstuvwxyzåäö'),'historisk snittränta')]]")
                ));
                scrollAndClick(driver, historyButton);

                WebElement historyTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//button[.//span[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ','abcdefghijklmnopqrstuvwxyzåäö'),'historisk snittränta')]]/following::table[1]")
                ));

                List<WebElement> rows = historyTable.findElements(By.cssSelector("tbody tr"));
                if (rows.isEmpty()) rows = historyTable.findElements(By.tagName("tr"));

                if (rows.isEmpty()) {
                    System.out.println("Ingen rad hittades i tabellen för historisk snittränta.");
                } else {
                    WebElement firstRow = rows.get(0);
                    // Läs både <th> och <td>
                    List<WebElement> cols = firstRow.findElements(By.xpath("./th|./td"));

                    if (cols.size() >= 3) {
                        String yearText = cols.get(0).getText().trim();
                        String monthText = cols.get(1).getText().trim();

                        String monthYear = monthText + " " + yearText;
                        YearMonth ym = ScraperUtils.parseSwedishMonth(monthYear);
                        LocalDate date = ym.atDay(1);
                        System.out.println("Historisk snittränta gäller: " + date + " (" + monthYear + ")");

                        // Rubriker för bindningstider
                        List<WebElement> headers = historyTable.findElements(By.cssSelector("thead th, thead td"));

                        for (int i = 2; i < cols.size(); i++) {
                            String headerText = headers.size() > i ? headers.get(i).getText() : "";
                            MortgageTerm term = ScraperUtils.parseTerm(headerText);
                            BigDecimal rate = ScraperUtils.parseRate(cols.get(i).getText());

                            if (term != null && rate != null) {
                                rates.add(new MortgageRate(bank, term, RateType.AVERAGERATE, rate, date));
                                System.out.println("→ Snittränta: " + term + " = " + rate + "%");
                            }
                        }
                        System.out.println("Hämtade historiska snitträntor för " + date + ".");
                    } else {
                        System.out.println("Tabellen hittades men hade för få kolumner.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Kunde inte hämta historisk snittränta: " + e.getMessage());
            }

            ScraperUtils.logResult("Landshypotek Bank", rates.size());

        } catch (Exception e) {
            System.err.println("Fel vid skrapning av Landshypotek Bank: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return rates;
    }

    // =================== Hjälpmetoder ===================

    /** Hanterar cookie-bannern (Cookiebot) */
    private void handleCookieBanner(WebDriver driver, WebDriverWait shortWait) {
        try {
            List<By> cookieButtons = List.of(
                    By.id("CybotCookiebotDialogBodyButtonAccept"),
                    By.id("CybotCookiebotDialogBodyButtonAllowAll"),
                    By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll"),
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
    }

    /** Scrollar till och klickar på ett element */
    private void scrollAndClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        element.click();
    }

    /** Extraherar räntor från tabell för angivet rateType */
    private void extractRatesFromTable(Bank bank, WebElement table, RateType rateType,
                                       List<MortgageRate> rates, LocalDate date) {
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        for (int i = 1; i < rows.size(); i++) { // hoppa över rubrikraden
            List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
            if (cols.size() < 2) continue;

            MortgageTerm term = ScraperUtils.parseTerm(cols.get(0).getText());
            BigDecimal rate = ScraperUtils.parseRate(cols.get(1).getText());

            if (term != null && rate != null) {
                rates.add(new MortgageRate(bank, term, rateType, rate, date));
            }
        }
    }
}