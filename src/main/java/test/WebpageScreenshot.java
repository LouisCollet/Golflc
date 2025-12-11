
package test;

import static interfaces.Log.LOG;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utils.LCUtil;

public class WebpageScreenshot {
    void main() {
        // Set the path to ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver.exe");
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();
        // Navigate to the webpage you want to capture
        driver.get("https://pescobilling.pk");
        try {
            // Take the screenshot and store it as a file
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Define the destination path for the screenshot
            File destinationFile = new File("screenshot.png");
            // Copy the screenshot to the destination path
            FileUtils.copyFile(screenshotFile, destinationFile);
            LOG.debug("Screenshot saved to: " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Exception in " + " / " + e);
            LCUtil.showMessageFatal("Exception = " + e.toString() );
        } finally {
            // Close the browser
            driver.quit();
        }
    }
}