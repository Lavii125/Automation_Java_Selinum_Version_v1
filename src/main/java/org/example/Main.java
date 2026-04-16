package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        String excelPath = "C:\\Users\\ttc1986\\IdeaProjects\\Automation_Java_Selinum\\coupons.xlsx";
        String myUsername = "xyz@kroger.com";
        String myPassword = "pass";

        System.setProperty("webdriver.edge.driver", "msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new EdgeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        try {
            // --- 1. OLD RELIABLE LOGIN SECTION ---
            driver.get("https://www.kroger.com/signin");
            System.out.println("Executing high-reliability login...");

            // Find Email Field
            By emailLoc = By.xpath("//input[@id='signInName' or @name='email' or @type='email']");
            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(emailLoc));

            // Force focus and type using Actions
            actions.moveToElement(emailInput).click().perform();
            Thread.sleep(1000);
            emailInput.sendKeys(myUsername);

            // JS Fallback for Username if SendKeys failed
            if (emailInput.getAttribute("value").isEmpty()) {
                js.executeScript("arguments[0].value='" + myUsername + "';", emailInput);
                js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", emailInput);
            }

            // Find and type Password
            By passLoc = By.xpath("//input[@type='password']");
            WebElement passwordInput = wait.until(ExpectedConditions.presenceOfElementLocated(passLoc));
            actions.moveToElement(passwordInput).click().perform();
            Thread.sleep(500);
            passwordInput.sendKeys(myPassword);

            // JS Fallback for Password
            if (passwordInput.getAttribute("value").isEmpty()) {
                js.executeScript("arguments[0].value='" + myPassword + "';", passwordInput);
                js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", passwordInput);
            }

            System.out.println("Credentials entered via JS/Actions. Clicking Sign In...");
            Thread.sleep(2000);

            // Use the "Continue" or "Sign In" button by ID or text
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
            js.executeScript("arguments[0].click();", submitBtn);

            // Wait for login to complete (important for SPA apps)
            Thread.sleep(8000);

            // --- 2. EXCEL PROCESSING SECTION (Columns D and F) ---
            System.out.println("Opening Excel file...");
            FileInputStream fis = new FileInputStream(new File(excelPath));
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int urlColIndex = 3;    // Column D
            int statusColIndex = 5; // Column F

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String couponUrl = formatter.formatCellValue(row.getCell(urlColIndex)).trim();

                if (couponUrl.isEmpty() || !couponUrl.startsWith("http")) {
                    System.out.println("Row " + (i + 1) + ": Skipping (No valid URL)");
                    continue;
                }

                System.out.println("Row " + (i + 1) + ": Navigating to " + couponUrl);
                driver.get(couponUrl);

                // Wait for the specific "unavailable" message
                Thread.sleep(4000);
                boolean isUnavailable = driver.findElements(By.xpath("//h4[contains(text(), 'unavailable') or contains(text(), 'not found')]")).size() > 0;

                Cell statusCell = row.getCell(statusColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String result = isUnavailable ? "N" : "Y";
                statusCell.setCellValue(result);
                System.out.println("   -> Result: " + result);
            }

            // --- 3. SAVE AND EXIT ---
            fis.close();
            try (FileOutputStream fos = new FileOutputStream(new File(excelPath))) {
                workbook.write(fos);
                System.out.println("\nSUCCESS! Excel file updated at: " + excelPath);
            }
            workbook.close();

        } catch (Exception e) {
            System.err.println("An error occurred:");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
