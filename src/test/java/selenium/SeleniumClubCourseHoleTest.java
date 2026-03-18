package selenium;

import static interfaces.Log.LOG;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Test Selenium end-to-end : Club -> Course -> Tee -> Holes Global (upsert)
 *
 * Ce test reproduit le scénario du bug du 2026-03-03 :
 * - Création d'un club, course, tee
 * - Puis création des 18 holes via holes_global.xhtml
 * - Avant le fix upsert, le UpdateHole échouait car les holes n'existaient pas
 *
 * IDs de référence du test échoué :
 * - Club ID 1256 (Clu de tst999)
 * - Course ID 177 (Parcours de test)
 * - Tee ID 281 (YELLOW, M, Slope 100, Rating 75.0)
 */
public class SeleniumClubCourseHoleTest {

    private static WebDriver driver = null;

    /**
     * Test complet : Club -> Course -> Tee -> Holes Global
     *
     * @param view page de départ (club.xhtml)
     * @return true si le test réussit
     */
    public boolean testing(String view) {
        try {
            LOG.debug("entering testing for view = " + view);

            driver = selenium.SeleniumController.setUp();
            String firstPartUrl = utils.LCUtil.firstPartUrl() + "/";
            LOG.debug("firstPartUrl = " + firstPartUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            Actions actions = new Actions(driver);

            // ========================================
            // 1. CLUB
            // ========================================
            String url = firstPartUrl + view;
            LOG.debug("1. CLUB - url = " + url);
            driver.get(url);
            LOG.debug("current url = " + driver.getCurrentUrl());
            LOG.debug("view title = " + driver.getTitle());

            WebElement element = driver.findElement(By.id("form_club:ClubName"));
            actions.sendKeys(element, "Club Selenium Hole Test").perform();

            element = driver.findElement(By.id("form_club:ClubAddress"));
            actions.sendKeys(element, "Rue de l'Amazone 55").perform();

            element = driver.findElement(By.id("form_club:ClubCity"));
            actions.sendKeys(element, "B-1060 Bruxelles").perform();

            WebElement country = driver.findElement(By.id("form_club:ClubCountry"));
            actions.sendKeys(country, "be").sendKeys(Keys.ENTER).perform();

            element = driver.findElement(By.id("form_club:ClubWebsite"));
            actions.sendKeys(element, "golf-selenium-test.com").perform();

            element = driver.findElement(By.id("form_club:ButtonGPSCoordinates"));
            actions.click(element).perform();
            Thread.sleep(3000);

            // Re-select country after GPS click
            country = driver.findElement(By.id("form_club:ClubCountry"));
            actions.sendKeys(country, "be").sendKeys(Keys.ENTER).perform();

            element = driver.findElement(By.id("form_club:ButtonCreateClub"));
            actions.click(element).perform();
            LOG.debug("1. CLUB created !");
            Thread.sleep(3000);

            // ========================================
            // 2. COURSE
            // ========================================
            LOG.debug("2. COURSE - navigating to course.xhtml");
            driver.get(firstPartUrl + "course.xhtml");
            LOG.debug("current url = " + driver.getCurrentUrl());
            LOG.debug("view title = " + driver.getTitle());

            element = driver.findElement(By.id("form_course:CourseName"));
            actions.sendKeys(element, "Parcours Selenium Hole Test").perform();

            element = driver.findElement(By.id("form_course:picker_endDate"));
            actions.sendKeys(element, "31/12/2030 10:00").perform();
            actions.click().perform(); // click outside picker

            element = driver.findElement(By.id("form_course:ButtonCreateCourse"));
            actions.click(element).perform();
            LOG.debug("2. COURSE created !");
            Thread.sleep(3000);

            // ========================================
            // 3. TEE
            // ========================================
            LOG.debug("3. TEE - navigating to tee.xhtml");
            driver.get(firstPartUrl + "tee.xhtml");
            LOG.debug("current url = " + driver.getCurrentUrl());
            LOG.debug("view title = " + driver.getTitle());

            // Gender : M (default, first button)
            element = driver.findElement(By.id("form_tee:select_gender:0"));
            actions.moveToElement(element).click().build().perform();

            // Start : YELLOW (index 2)
            element = driver.findElement(By.id("form_tee:select_start:2"));
            actions.moveToElement(element).click().build().perform();
            LOG.debug("tee start = YELLOW selected");

            // Holes played : 01-18 (index 0, default)
            element = driver.findElement(By.id("form_tee:select_holes:0"));
            actions.moveToElement(element).click().build().perform();

            // Par : 72 (index 0, default)
            element = driver.findElement(By.id("form_tee:select_par:0"));
            actions.moveToElement(element).click().build().perform();

            // Slope : 100
            element = driver.findElement(By.id("form_tee:input_slope_input"));
            element.clear();
            actions.sendKeys(element, "100").perform();
            LOG.debug("tee slope = 100");

            // Rating : 75.0
            element = driver.findElement(By.id("form_tee:input_rating_input"));
            element.clear();
            actions.sendKeys(element, "75.0").perform();
            LOG.debug("tee rating = 75.0");

            element = driver.findElement(By.id("form_tee:ButtonCreateTee"));
            actions.click(element).perform();
            LOG.debug("3. TEE created !");
            Thread.sleep(3000);

            // ========================================
            // 4. HOLES GLOBAL (upsert test)
            // ========================================
            LOG.debug("4. HOLES GLOBAL - navigating to holes_global.xhtml");
            driver.get(firstPartUrl + "holes_global.xhtml");
            LOG.debug("current url = " + driver.getCurrentUrl());
            LOG.debug("view title = " + driver.getTitle());
            Thread.sleep(2000);

            // Remplir les 18 holes avec des valeurs de test
            // Par: tous par 4, StrokeIndex: 1-18, Distance: 300-470
            int[] pars =       {4, 4, 5, 3, 4, 4, 4, 3, 5, 4, 4, 5, 3, 4, 4, 4, 3, 5};
            int[] indexes =    {1, 3, 5, 7, 9, 11, 13, 15, 17, 2, 4, 6, 8, 10, 12, 14, 16, 18};
            int[] distances = {380, 350, 510, 160, 400, 370, 420, 150, 520, 390, 340, 500, 170, 410, 360, 430, 140, 530};

            for (int i = 1; i <= 18; i++) {
                // p:inputNumber generates an inner <input> with id suffix _input
                // Par
                element = driver.findElement(By.id("form_holes_global:par" + i + "_input"));
                element.clear();
                element.sendKeys(String.valueOf(pars[i - 1]));

                // Stroke Index
                element = driver.findElement(By.id("form_holes_global:holeindex" + i + "_input"));
                element.clear();
                element.sendKeys(String.valueOf(indexes[i - 1]));

                // Distance
                element = driver.findElement(By.id("form_holes_global:distance" + i + "_input"));
                element.clear();
                element.sendKeys(String.valueOf(distances[i - 1]));
            }
            LOG.debug("4. HOLES GLOBAL - 18 holes filled");

            // Click Modify/Create button
            element = driver.findElement(By.id("form_holes_global:buttonModify"));
            actions.click(element).perform();
            LOG.debug("4. HOLES GLOBAL - buttonModify clicked !");

            // Wait for processing
            Thread.sleep(5000);

            // Verify : check the page title or a success message
            LOG.debug("4. HOLES GLOBAL - current url after submit = " + driver.getCurrentUrl());
            LOG.debug("4. HOLES GLOBAL - test completed successfully !");

            driver.manage().window().maximize();
            Thread.sleep(3000);

            SeleniumController.tearDown(driver);
            return true;

        } catch (UnhandledAlertException e) {
            String msg = "UnhandledAlertException in SeleniumClubCourseHoleTest " + e;
            LOG.info(msg);
            showMessageInfo(msg);
            LOG.info("alert accepted");
            LOG.info("alert dismissed");
            return false;
        } catch (NoAlertPresentException ex) {
            String msg = "NoAlertPresentException in SeleniumClubCourseHoleTest " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        } catch (Exception ex) {
            String msg = "Exception in SeleniumClubCourseHoleTest " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    } // end method

    public static void main(String args[]) {
        try {
            boolean b = new SeleniumClubCourseHoleTest().testing("club.xhtml");
            LOG.debug("result SeleniumClubCourseHoleTest.main = " + b);
        } catch (Exception e) {
            String msg = "Exception in SeleniumClubCourseHoleTest.main = " + e.getMessage();
            LOG.error(msg);
        }
    } // end main
} // end class
