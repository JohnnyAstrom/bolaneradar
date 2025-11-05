package com.bolaneradar.backend.service.scraper;

import com.bolaneradar.backend.model.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Webbskrapare för Skandia.
 * Hämtar både aktuella (listräntor) och genomsnittliga (snitträntor) bolåneräntor.
 * <p>
 * Snitträntor får korrekt månad och år från rubriken "Snitträntor oktober 2025".
 */
@Service
public class SkandiabankenScraper implements BankScraper {

    private static final String URL = "https://www.skandia.se/lana/bolan/bolanerantor/";
    private static final Random RAND = new Random();

    @Override
    public List<MortgageRate> scrapeRates(Bank bank) {
        System.out.println("🏦 Startar skrapning för Skandia...");
        List<MortgageRate> rates = new ArrayList<>();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--window-size=1366,1200");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        try {
            driver.get(URL);

            // === 1️⃣ Stäng cookie-popup ===
            List<By> cookieSelectors = List.of(
                    By.id("onetrust-accept-btn-handler"),
                    By.cssSelector("button[id*='accept']"),
                    By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acceptera')]"),
                    By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'godkänn')]")
            );

            boolean cookieClosed = false;
            for (By sel : cookieSelectors) {
                try {
                    WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(4))
                            .until(ExpectedConditions.elementToBeClickable(sel));
                    btn.click();
                    Thread.sleep(300 + RAND.nextInt(300));
                    System.out.println("[Skandia] Cookie-dialog stängd.");
                    cookieClosed = true;
                    break;
                } catch (Exception ignored) {}
            }

            if (!cookieClosed) {
                ((JavascriptExecutor) driver).executeScript("""
                    var el = document.querySelector("div#onetrust-banner-sdk, div[class*='cookie']");
                    if (el) el.remove();
                """);
                System.out.println("[Skandia] Ingen cookie-popup eller borttagen via JS.");
            }

            // === 2️⃣ Simulera enkel interaktion för att trigga rendering ===
            try {
                Actions actions = new Actions(driver);
                actions.moveByOffset(100, 100).perform();
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, document.body.scrollHeight / 3);");
                Thread.sleep(500);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(700);
            } catch (Exception ignored) {}

            // === 3️⃣ Vänta in tabeller ===
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody tr")));
            List<WebElement> tables = driver.findElements(By.cssSelector("table"));
            System.out.println("[Skandia] Hittade " + tables.size() + " tabeller.");

            // === 4️⃣ Hämta datum för snitträntor från rubriken "Snitträntor oktober 2025" ===
            LocalDate averageDate = extractAverageDate(driver);
            if (averageDate != null) {
                System.out.println("[Skandia] Snitträntor gäller " + averageDate);
            }

            // === 5️⃣ Extrahera tabeller ===
            int tableIndex = 1;
            for (WebElement table : tables) {
                RateType rateType = (tableIndex == 1) ? RateType.AVERAGERATE : RateType.LISTRATE;
                LocalDate date = (rateType == RateType.AVERAGERATE && averageDate != null)
                        ? averageDate
                        : LocalDate.now();

                for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() < 2) continue;

                    String termText = cols.get(0).getText().toLowerCase().trim();
                    String rateText = cols.get(1).getText().replace("%", "").replace(",", ".").trim();

                    MortgageTerm term = ScraperUtils.parseTerm(termText);
                    BigDecimal rate = ScraperUtils.parseRate(rateText);

                    if (term != null && rate != null) {
                        rates.add(new MortgageRate(bank, term, rateType, rate, date));
                        System.out.println("[Skandia] " + rateType + " | " + termText + " = " + rate + "% (" + date + ")");
                    }
                }
                tableIndex++;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Fel vid Skandia-scraping: " + e.getMessage());
        } finally {
            driver.quit();
        }

        System.out.println("✅ Skandia: totalt " + rates.size() + " räntor hittade.");
        return rates;
    }

    /** Hämtar datum från rubriken, t.ex. "Snitträntor oktober 2025" */
    private LocalDate extractAverageDate(WebDriver driver) {
        try {
            List<WebElement> headings = driver.findElements(By.xpath("//*[contains(text(),'Snitträntor')]"));
            for (WebElement h : headings) {
                String text = h.getText().toLowerCase().trim();
                if (text.matches(".*\\b(januari|februari|mars|april|maj|juni|juli|augusti|september|oktober|november|december)\\b\\s+\\d{4}")) {
                    String monthYear = text.replaceAll(".*snitträntor\\s*", "").trim();
                    return parseMonthYear(monthYear);
                }
            }
        } catch (Exception e) {
            System.err.println("[Skandia] Kunde inte hitta snitträntedatum: " + e.getMessage());
        }
        return null;
    }

    /** Konverterar t.ex. "oktober 2025" till LocalDate (2025-10-01) */
    private LocalDate parseMonthYear(String text) {
        try {
            String[] parts = text.split("\\s+");
            if (parts.length >= 2) {
                String monthName = parts[0].toLowerCase();
                int year = Integer.parseInt(parts[1]);
                int month = switch (monthName) {
                    case "januari","january" -> 1;
                    case "februari","february" -> 2;
                    case "mars","march" -> 3;
                    case "april" -> 4;
                    case "maj","may" -> 5;
                    case "juni","june" -> 6;
                    case "juli","july" -> 7;
                    case "augusti","august" -> 8;
                    case "september" -> 9;
                    case "oktober","october" -> 10;
                    case "november" -> 11;
                    case "december" -> 12;
                    default -> 1;
                };
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            System.err.println("[Skandia] Kunde inte tolka månad/år: " + text);
        }
        return LocalDate.now();
    }
}