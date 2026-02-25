
package Controllers;

import static interfaces.GolfInterface.ZDF_FILE;
import static interfaces.Log.LOG;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.enterprise.context.RequestScoped;
import org.openqa.selenium.chrome.ChromeDriver;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.openqa.selenium.Alert;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.CapabilityType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

//@Named("activationC")
@RequestScoped //@SessionScoped mod 26-08-2023
public class SeleniumController implements Serializable{
public SeleniumController(){ }// constructor

public static WebDriver setUp(){
try{
       LOG.debug("entering WebDriver setUp()");
   
  // https://developer.chrome.com/docs/chromedriver/capabilities?hl=fr#:~:text=You%20can%20create%20an%20instance,to%2Fextension.crx%22))%3B  
//Setting up options to run our test script
   ChromeOptions chromeOptions = new ChromeOptions();
 //  options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
   chromeOptions.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT); // important !!
   chromeOptions.setCapability("browserName", "chrome");
   chromeOptions.addArguments("start-maximized");  // Utiliser Chrome en mode plein écran
   chromeOptions.addArguments("--start-maximized"); //  faut les -- ?
   chromeOptions.addArguments("disable-extensions"); 
   chromeOptions.setExperimentalOption("excludeSwitches",Arrays.asList("disable-popup-blocking")); // Bloquer les fenêtres de dialogue
   chromeOptions.setExperimentalOption("useAutomationExtension", false); // how to hide the "Chrome is being controlled by automated software..." 
   chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
   //https://www.lambdatest.com/blog/webdrivermanager-in-selenium/
   
 //  chromeOptions.setPlatformName("Windows 10");
    LOG.debug("entering line 01");
// new 18-05-2024
//   options.addArguments("disable-infobars");
   chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL); // wait for all resources
///   options.addArguments("window-size=1920x1080");
 //  options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
 //   WebDriver driverInstance = new ChromeDriver(); // important
 LOG.debug("entering WebDriver line 00, options Name = " + chromeOptions.getBrowserName());
// LOG.debug("entering WebDriver line 00, options version = " + chromeOptions.getBrowserVersion());
   System.setProperty("java.awt.headless", "false"); // YES YES !!eviter erreur = java.awt.AWTException: headless environment 
  /*/Combining DesiredCapabilities with Options class: Use the merge(Capabilities) method of the Options class.
     DesiredCapabilities capabilities = new DesiredCapabilities();
     capabilities.setCapability(ChromeOptions.CAPABILITY, options);
     capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
     options.merge(capabilities);
 */    
    WebDriver driverInstance = new ChromeDriver(chromeOptions);
    WebDriverManager.chromedriver().setup();
 //   SeleniumController.setUp();
      LOG.debug("driverInstance = " + driverInstance.toString());
    driverInstance.manage().window().maximize();
    // alternative : on voir ce qu'on fait 
    Dimension dimension = new Dimension(600,800);
    driverInstance.manage().window().setSize(dimension);
 //      LOG.debug("entering WebDriver line 01");
    //https://www.browserstack.com/guide/wait-commands-in-selenium-webdriver
    driverInstance.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));  // mod 04-06-2024 was 5
  //  Duration dur = driver.manage().timeouts().getImplicitWaitTimeout();
  //    LOG.debug("duration parsed in seconds = " + dur.getSeconds());
 // https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/WebDriver.Timeouts.html
       LOG.debug("Selenium BuildInfo releaseLabel = " + new BuildInfo().getReleaseLabel() + " rev = " + new BuildInfo().getBuildRevision());
//    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
//    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
   if(!GraphicsEnvironment.isHeadless()){
                LOG.debug("is NOT headless ! " );
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice(); // if multi-monitor configuration fonction si headless = false
         //   var v = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices(); 
         //       LOG.debug("array screendevices = " + Arrays.toString(v));
            double width = gd.getDisplayMode().getWidth(); 
                LOG.debug("     width = " + width);
            double heigth = gd.getDisplayMode().getHeight(); 
                LOG.debug("     heigth = " + heigth);
       }else{
           LOG.debug("is headless ! ");
       }

        LOG.debug("exiting setUp()");
   return driverInstance;
   }catch (Exception ex){
            String msg = "Exception in setUp() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
}  // end method setUp()   

/* Function to Make border
   private void MakeBorder(WebElement Element){
        JavascriptExecutor js = (JavascriptExecutor)driverInstance;
        js.executeScript(
            "arguments[0].style.border = '3px solid red'",
            Element);
    }
   */
 public static boolean tearDown(WebDriver driver){
       //https://www.simplilearn.com/tutorials/selenium-tutorial/what-is-selenium-webdriver
try{
    LOG.debug("entering tearDown");
    driver.close();
       LOG.debug("driver is closed");
    driver.quit();
       LOG.debug("driver is quit");
    return true;
 }catch (Exception ex){
            String msg = "Exception in tearDown() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;   
}
   }

 public static boolean capture(String view) throws IOException {
  try{
      // mod 31/10/2024
      LOG.debug("entering capture() with view = " + view);
       String url = utils.LCUtil.firstPartUrl() + view;
          LOG.debug("entering capture() with url = " + url);
       WebDriver driverInstance = SeleniumController.setUp(); // mod 31/10/2024
       driverInstance.get(url);
       File screenshotFile = ((TakesScreenshot)driverInstance).getScreenshotAs(OutputType.FILE);
             LOG.debug("screenshotFile space = " + screenshotFile.getTotalSpace()); 
             LOG.debug("user.dir  = " + System.getProperty("user.dir")); // write into wildfly/bin !!
    /// not available in run      LOG.debug(" to be saved in " + Settings.getProperty("MAIL")); // Settings.getProperty("USER_DIR")
          File destinationFile = new File("selenium-"  + view.substring(1) + "-" + System.currentTimeMillis() + ".png");  // remove / of view
     //     File destinationFile = new File("selenium-"+ LocalDateTime.now().format(ZDF_FILE) + ".png");
   //    File destinationFile = new File("selenium-" + view + LocalDateTime.now().format(ZDF_FILE) + ".png");
            LOG.debug(" destinationFile = " + destinationFile);
       FileHandler.copy(screenshotFile, destinationFile);
      //      LOG.debug("Took Screenshot for " + site + " and saved as " + "site" + screenshotNum + ".png");
            LOG.debug("Screenshot saved to: " + destinationFile.getAbsolutePath());
            //https://www.selenium.dev/documentation/webdriver/interactions/windows/
  /* à essayer
            WebElement element = driver.findElement(By.cssSelector("h1"));
            File scrFile = element.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("./image.png"));
   import org.openqa.selenium.print.PrintOptions;
https://www.selenium.dev/documentation/webdriver/interactions/print_page/
    driver.get("https://www.selenium.dev");
    printer = (PrintsPage) driver;

    PrintOptions printOptions = new PrintOptions();
    printOptions.setPageRanges("1-2");
        String[] current_range = printOptions.getPageRanges();
    printOptions.setOrientation(PrintOptions.Orientation.LANDSCAPE);
    PrintOptions.Orientation current_orientation = printOptions.getOrientation();        
    Pdf pdf = printer.print(printOptions);
    String content = pdf.getContent();
     */       
  
      //      File src = webElement.getScreenshotAs(OutputType.FILE);
       //     File destinationFile2 = new File("selenium-"+ LocalDateTime.now().format(ZDF_FILE) + ".png");
       //    LOG.debug(" destinationFile2 " + destinationFile2.getName());
       //    FileHandler.copy(src, destinationFile2);
     //      driver.switchTo().frame(iframeElement);
      // full screen scrolling needed   
         Screenshot aScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(driverInstance);
         ImageIO.write(aScreenshot.getImage(), "PNG", new File("selenium-FullScreen-"+ LocalDateTime.now().format(ZDF_FILE) + ".png"));
          LOG.debug("taken with Ashot() = C:/log/FullView.png "); // + destinationFile.getAbsolutePath());
  //     GraphicsEnvironment.isHeadless());
         LOG.debug("exiting capture()");
    //     return tearDown(driverInstance);  // pas de sens est void
         return true;
 } catch ( UnhandledAlertException e) {   // UnexpectedAlertPresentException
    String msg = "UnhandledAlertException in capture " + e;
    LOG.info(msg);
    showMessageInfo(msg);
//    Alert alert = driverInstance.switchTo().alert();
  //  String alertText = alert.getText();
 //     LOG.debug("ERROR: (ALERT BOX DETECTED) - ALERT MSG : " + alert.getText());
 //   alert.accept();
      LOG.info("alert accepted");
 //   alert.dismiss(); //it is important you close alert or do something about it. 
      LOG.info("alert dismissed");
      return false;
}catch (NoAlertPresentException ex){
            String msg = "NoAlertPresentException in capture " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}catch (Exception ex){
            String msg = "Exception in capture " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
  }
 
void main(){
  try{
    SeleniumController ex1 = new SeleniumController();
 //   ex1.capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/technical_info.xhtml");
//    ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
    

 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in ExampleOne.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//
} // end class