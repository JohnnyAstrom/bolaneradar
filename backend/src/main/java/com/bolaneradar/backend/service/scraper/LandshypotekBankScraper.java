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
            extractRatesFromTable(bank, listRateTable, RateType.LISTRATE, rates, LocalDate.now());
            System.out.println("Hämtade listräntor.");

            // === 3️⃣ Klicka upp "Snitträntor för bolån senaste månaden" ===
            WebElement avgRateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Snitträntor')]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", avgRateButton);
            avgRateButton.click();

            // 🟢 Hämta månadsrubriken precis ovanför tabellen (t.ex. "Oktober")
            String monthText = "";
            try {
                WebElement monthElement = driver.findElement(By.xpath("//button[contains(.,'Snitträntor')]/following::p[1]"));
                monthText = monthElement.getText().toLowerCase().trim();
                System.out.println("Snitträntor gäller månad: " + monthText);
            } catch (Exception ignored) {}

            // 🟢 Konvertera till LocalDate
            LocalDate avgDate = parseMonthToDate(monthText);

            WebElement avgRateTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[contains(.,'Snitträntor')]/following::table[1]")
            ));
            extractRatesFromTable(bank, avgRateTable, RateType.AVERAGERATE, rates, avgDate);
            System.out.println("Hämtade snitträntor för " + avgDate + ".");

            System.out.println("Landshypotek Bank: totalt " + rates.size() + " räntor hittade.");

        } catch (Exception e) {
            System.err.println("Fel vid skrapning av Landshypotek Bank: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return rates;
    }

    private void extractRatesFromTable(Bank bank, WebElement table, RateType rateType, List<MortgageRate> rates, LocalDate date) {
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
                rates.add(new MortgageRate(bank, term, rateType, rate, date));
                System.out.println(rateType + " | " + termText + " = " + rate + "% (" + date + ")");
            }
        }
    }

    private LocalDate parseMonthToDate(String monthText) {
        int month = switch (monthText) {
            case "januari" -> 1;
            case "februari" -> 2;
            case "mars" -> 3;
            case "april" -> 4;
            case "maj" -> 5;
            case "juni" -> 6;
            case "juli" -> 7;
            case "augusti" -> 8;
            case "september" -> 9;
            case "oktober" -> 10;
            case "november" -> 11;
            case "december" -> 12;
            default -> LocalDate.now().getMonthValue();
        };

        int year = LocalDate.now().getYear();

        // 🟢 Justera om vi är i början av året och sidan visar "januari" (föregående år)
        if (month == 12 && LocalDate.now().getMonthValue() == 1) {
            year -= 1;
        }

        // 🟢 Eftersom Landshypotek alltid visar *föregående månad*, dra bort en månad
        LocalDate baseDate = LocalDate.of(year, month, 1);
        return baseDate.minusMonths(1);
    }
}