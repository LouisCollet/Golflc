package calc;

import static io.github.bonigarcia.wdm.WebDriverManager.isOnline;
import static interfaces.Log.LOG;
import static io.github.bonigarcia.seljup.Browser.CHROME;
import io.github.bonigarcia.seljup.DriverCapabilities;
import io.github.bonigarcia.seljup.EnabledIfBrowserAvailable;
import io.github.bonigarcia.seljup.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.seljup.SeleniumJupiter;
import io.github.bonigarcia.seljup.SingleSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v133.page.Page.GetLayoutMetricsResponse;
import org.openqa.selenium.devtools.v133.dom.model.Rect;
import org.openqa.selenium.devtools.v133.page.Page;
import org.openqa.selenium.devtools.v133.page.model.Viewport;
import org.openqa.selenium.interactions.Actions;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static utils.LCUtil.showMessageFatal;
// https://bonigarcia.dev/selenium-jupiter/



@EnabledIfBrowserAvailable(CHROME) // the test is skipped when the browser specified as a parameter is not available in the system.
@ExtendWith(SeleniumJupiter.class)
@Execution(ExecutionMode.CONCURRENT)

@TestMethodOrder(OrderAnnotation.class)
@SingleSession // garder le driver open ??
class SeleniumJupiterTest {
    static String firstPartUrl = "";
    static SessionId sessionId;
  
 //    @Options() // To configure browser options (e.g. ChromeOptions for Chrome and Opera,
    
 @BeforeAll
    static void setup() {
        // Resolve driver
        LOG.debug("entering BeforeAll - setup");
        firstPartUrl = utils.LCUtil.firstPartUrl() + "/";
        LOG.debug("exiting setup with firstPartUrl = " + firstPartUrl);
}
 @AfterAll
    static void tearDown() {
        // Resolve driver
        LOG.debug("entering AfterAll - tearDown");
    //    driver.close(); automatic 
        LOG.debug("exiting cleanup = ");
}    
    
    
  @DriverCapabilities
  ChromeOptions options = new ChromeOptions();

  @BeforeEach
    void setup2() {
        LOG.debug("entering BeforeEach - setup2");
        options.setCapability(CapabilityType.PLATFORM_NAME, "Android");
        options.setExperimentalOption("useAutomationExtension", false); // ne fonctionne pas ici !! how to hide the "Chrome is being controlled by automated software..." 
    //    options.setCapability(EspressoOptions.DEVICE_NAME_OPTION, "Nexus 5 API 30");
    //    options.setCapability(EspressoOptions.AUTOMATION_NAME_OPTION, "UiAutomator2");
    } // end
    
 // assertThat(driver.getTitle()).contains("Selenium WebDriver");
 //   sessionId = driver.getSessionId();
@DisplayName("success for order 1 !")   
@Test
@Order(1)  
 void test_club(ChromeDriver driver) {
  try{
     LOG.debug("entering test_club - order 1");
    //   ChromeOptions options = new ChromeOptions();

      String url = firstPartUrl + "club.xhtml";
 //assumeThat(isOnline(new URL(url, "/status"))).isTrue();
 // URL appiumServerUrl = new URL("http://localhost:4723");
       //   String url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + view;   // run testing
      driver.get(url);
         LOG.debug("current url = " + driver.getCurrentUrl());
    //      LOG.debug("Driver : page source = " + "\n" + driver.getPageSource()); // pour faire les recherches !
            LOG.debug("view title = " + driver.getTitle());
          assertThat(driver.getTitle()).contains("Club Page");
            LOG.debug("after assertThat contains " + driver.getTitle());
      //     assertThat(driver.getTitle()).info.equals("Club Page");
      //     LOG.debug("after assertThat equals " + driver.getTitle());
           Assertions.assertThat(driver.getTitle()).isEqualTo("Club Page");
            LOG.debug("after assertThat isEqualTo " + driver.getTitle());

        Actions actions = new Actions(driver); // il n'y a que Actions qui fonctionne !! click ne fonctionne pas toujours!!

        WebElement element = driver.findElement(By.id("form_club:ClubName"));  // faut préfixe = id de form form_tee:select_gender
        assertTrue(element.isDisplayed());
        actions.sendKeys(element, "Club de test via Selenium-Jupiter").perform();
       
        element = driver.findElement(By.id("form_club:ClubAddress"));
        actions.sendKeys(element, "Rue de l'Amazone 55").perform();

        element = driver.findElement(By.id("form_club:ClubCity"));
        actions.sendKeys(element, "B-1060 Bruxelles").perform();

        WebElement country = driver.findElement(By.id("form_club:ClubCountry"));
        actions.sendKeys(country, "be").sendKeys(Keys.ENTER); // faut forceSelection="false" dans autocomplete du .xhtml

   //      Thread.sleep(1000);
        element = driver.findElement(By.id("form_club:ClubWebsite"));
        actions.sendKeys(element, "www.fairmont.com").perform();
         
         // 26-03-2025 faut accepter cookies voir document word Primefaces selenium.docx uniquement la première fois après les cookies sont acceptés
     //   element = driver.findElement(By.id("aspnetForm:onetrust-accept-btn-handler"));
     //    actions.click(element).perform();
         
        element = driver.findElement(By.id("form_club:ButtonGPSCoordinates"));
        actions.click(element).perform();

        element = driver.findElement(By.id("form_club:ButtonCreateClub"));
        actions.click(element).perform();

          LOG.debug("club created !"); // valeur de clubid ??
        Thread.sleep(5000);
   //By.partialLinkText    https://www.guru99.com/locate-by-link-text-partial-link-text.html 
     //   LOG.debug("driver getTitle = " + driver.getTitle());
     //   assertThat(driver.getTitle()).contains("Club Page");
     //   WebElement form = driver.findElement(By.partialLinkText("form"));
     //   
}catch (InterruptedException ex){
            String msg = "Exception in test_club " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
          //  return false;
    }
 }// end method
@Test
@Order(2)        
 void test_course(ChromeDriver driver) {
  try{
     LOG.debug("entering test_course - order 2");
      String url = firstPartUrl + "course.xhtml";
      driver.get(url);
         LOG.debug("current url = " + driver.getCurrentUrl());
         LOG.debug("view title = " + driver.getTitle());
    assertThat(driver.getTitle()).contains("Course Page");
            LOG.debug("after assertThat contains " + "Course Page");
    Assertions.assertThat(driver.getTitle()).isEqualTo("Course Page");
            LOG.debug("after assertThat isEqualTo " + "Course Page");
            
         Actions actions = new Actions(driver); // il n'y a que Actions qui fonctionne !! click ne fonctionne pas toujours!!

        WebElement element = driver.findElement(By.id("form_course:CourseName"));
        actions.sendKeys(element, "Course de test selenium").perform();

          // https://www.scaler.com/topics/selenium-tutorial/how-to-handle-date-picker-in-selenium/  
        element = driver.findElement(By.id("form_course:picker_endDate"));
        actions.sendKeys(element,"07/07/2033 10:45").perform();  // Assuming the date format is MM/DD/YYYY HH:mm, faut readOnly="false" dans p:datePicker
        actions.click(); // click en dehors picker !!

        element = driver.findElement(By.id("form_course:ButtonCreateCourse"));
        actions.click(element).perform();
             LOG.debug("course created !"); // failed because clubid is unknown !!
         Thread.sleep(5000);
}catch (InterruptedException ex){
            String msg = "Exception in test_course " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
    }
 }// end method
 
@Test
@Order(3)        
 void test_tee(ChromeDriver driver) {
  try{
     LOG.debug("entering test_tee - order 3");
      String url = firstPartUrl + "tee.xhtml";
      driver.get(url);
         LOG.debug("current url = " + driver.getCurrentUrl());
         LOG.debug("view title = " + driver.getTitle());
    assertThat(driver.getTitle()).contains("Tee Page");
            LOG.debug("after assertThat contains" + "Tee Page");
    Assertions.assertThat(driver.getTitle()).isEqualTo("Tee Page");
            LOG.debug("after assertThat isEqualTo " + "Tee Page");
            
       Actions actions = new Actions(driver);
       WebElement element = driver.findElement(By.id("form_tee:select_gent"));  // dans tee.xhtml =  p:selectOneButton id="select_gender"
    //    actions.sendKeys(element,"L").perform(); // Ladies
       actions.click(element).perform();
        
        
        LOG.debug("line 00");
 // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
 // element = wait.until(ExpectedConditions.elementToBeClickable(By.id("form_tee:select_start:1")));
         element = driver.findElement(By.id("form_tee:select_start:2"));
    //     actions.sendKeys(element,"YELLOW").perform();
    //https://www.lambdatest.com/blog/element-is-not-clickable-at-point-exception/
         actions.moveToElement(element).click().build().perform();
     //   WebElement radio1 = driver.findElement(By.id("form_tee:select_start:1"));
     //    radio1.click();
            LOG.debug("select start clicked");
      //   
       //  objSelect.selectByVisibleText("BLACK");
//   LOG.debug("line 02");       
     //    List <WebElement> elementCount = objSelect.getOptions();
  // LOG.debug("nombre element select_start" + elementCount.size());
         
         element = driver.findElement(By.id("form_tee:select_holes:1"));
     //    actions.sendKeys(element, "01-09").perform();
         actions.moveToElement(element).click().build().perform();
            LOG.debug("tee holes ok");   
         element = driver.findElement(By.id("form_tee:select_par:1"));
         actions.sendKeys(element, "71").perform();
            LOG.debug("par par ok"); 
         element = driver.findElement(By.id("form_tee:input_slope"));  
         actions.sendKeys(element, "129").perform();
            LOG.debug("par slope ok");
         element = driver.findElement(By.id("form_tee:input_rating"));
         actions.sendKeys(element, "70.5").perform();

         element = driver.findElement(By.id("form_tee:ButtonCreateTee"));
         actions.click(element).perform();
             LOG.debug("tee created !");
        driver.manage().window().maximize();
        Thread.sleep(15000);

}catch (Exception ex){
        String msg = "Exception in test_course " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
    }
 }// end method
 
@Disabled("not implemented yet")
@Test
@Order(4)
    void testFullPageScreenshot(ChromeDriver driver, DevTools devTools) throws IOException {
          LOG.debug("entering testFullPageScreenshot order 2");
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/long-page.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(
                By.className("container"), By.tagName("p")));
        
        GetLayoutMetricsResponse metrics = devTools.send(Page.getLayoutMetrics());
        Rect contentSize = metrics.getContentSize();
        String screenshotBase64 = devTools
                .send(Page.captureScreenshot(Optional.empty(), Optional.empty(),
                        Optional.of(new Viewport(0, 0, contentSize.getWidth(),
                                contentSize.getHeight(), 1)),
                        Optional.empty(), Optional.of(true), Optional.empty()));
        Path destination = Paths.get("fullpage-screenshot-chrome.png");
        Files.write(destination, Base64.getDecoder().decode(screenshotBase64));

        assertThat(destination).exists();
           LOG.debug("exiting testFullPageScreenshot order 2");
    }
} //end Class