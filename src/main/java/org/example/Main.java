package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {

        // 1. Manually point to your driver
        System.setProperty("webdriver.edge.driver", "msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        // Helps prevent the browser from being detected as a bot by some corporate firewalls
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new EdgeDriver(options);

        try {
            // 2. Navigate to Kroger
            System.out.println("Navigating... PLEASE ENTER PROXY CREDENTIALS NOW.");
            driver.get("https://www.kroger.com");

            // 3. Setup a long wait for the initial proxy/page load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(90));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 4. THE POP-UP CLOSE LOGIC (Using JavaScript)
            System.out.println("Attempting to clear the pop-up...");
            try {
                // Wait for the button to exist in the DOM
                By closeBtnPath = By.xpath("//button[@aria-label='Close pop-up']");
                WebElement closeButton = wait.until(ExpectedConditions.presenceOfElementLocated(closeBtnPath));

                // Force a JavaScript click
                js.executeScript("arguments[0].click();", closeButton);
                System.out.println("Pop-up closed via JavaScript.");

                // Ensure the pop-up is gone before proceeding
                wait.until(ExpectedConditions.invisibilityOfElementLocated(closeBtnPath));
            } catch (Exception e) {
                System.out.println("Pop-up button not clickable or didn't appear. Proceeding to main content.");
            }

            // 5. THE MAIN ACTION: Click Digital Coupons
            System.out.println("Locating and clicking Digital Coupons...");

            // Using the data-testid from your HTML
            By couponsPath = By.xpath("//li[@data-testid='MarqueeLinks-Digital Coupons']/a");
            WebElement digitalCouponsLink = wait.until(ExpectedConditions.elementToBeClickable(couponsPath));

            // Again, use JS click here just in case another pop-up/overlay is in the way
            js.executeScript("arguments[0].click();", digitalCouponsLink);

            System.out.println("Successfully clicked 'Digital Coupons'!");

            // 6. Verify URL change
            wait.until(ExpectedConditions.urlContains("coupons"));
            System.out.println("Automation complete. Current URL: " + driver.getCurrentUrl());

            // Stay open so you can see the result
            Thread.sleep(10000);

        } catch (Exception e) {
            System.err.println("Automation failed during the process.");
            e.printStackTrace();
        } finally {
            // driver.quit(); // Keep open for manual review
            System.out.println("Script ended. Browser is still active.");
        }
    }
}