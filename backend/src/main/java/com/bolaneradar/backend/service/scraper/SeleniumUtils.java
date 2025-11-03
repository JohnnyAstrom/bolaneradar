package com.bolaneradar.backend.service.scraper;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class SeleniumUtils {

    /** Standardinställningar för alla Selenium-scrapers. */
    public static ChromeOptions createDefaultOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");

        try {
            Path tempProfile = Files.createTempDirectory("selenium-profile-");
            options.addArguments("--user-data-dir=" + tempProfile.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Kunde inte skapa temporär Selenium-profil: " + e.getMessage());
        }

        return options;
    }

    /** Försöker klicka bort cookiebanners, både direkt och i iframes. */
    public static void acceptCookies(WebDriver driver, WebDriverWait wait) {
        List<By> selectors = List.of(
                By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll"),
                By.cssSelector("button[id*='accept']"),
                By.cssSelector("button[class*='cookie']"),
                By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acceptera')]"),
                By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'godkänn')]"),
                By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]")
        );

        for (By by : selectors) {
            if (tryClick(wait, by)) return;
        }

        // Försök i iframe
        for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
            try {
                driver.switchTo().frame(iframe);
                for (By by : selectors) {
                    if (tryClick(wait, by)) {
                        driver.switchTo().defaultContent();
                        return;
                    }
                }
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {
                driver.switchTo().defaultContent();
            }
        }

        // JS fallback
        jsFallbackClick(driver);
    }

    /** Enkel scroll för att trigga rendering. */
    public static void scrollPage(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(500);
        } catch (Exception ignored) {}
    }

    private static boolean tryClick(WebDriverWait wait, By by) {
        try {
            WebElement el = wait.withTimeout(Duration.ofSeconds(4))
                    .until(ExpectedConditions.elementToBeClickable(by));
            el.click();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean jsFallbackClick(WebDriver driver) {
        try {
            String js =
                    "var texts=['acceptera','godkänn','accept'];" +
                            "var btns=[...document.querySelectorAll('button,[role=\"button\"],input[type=\"button\"],input[type=\"submit\"]')];" +
                            "for(let b of btns){let t=(b.innerText||b.value||'').toLowerCase();" +
                            "if(texts.some(k=>t.includes(k))){b.click();return true;}}return false;";
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(js));
        } catch (Exception ignored) {
            return false;
        }
    }
}