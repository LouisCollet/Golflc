package selenium;

import static interfaces.GolfInterface.ZDF_FILE;
import static interfaces.Log.LOG;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.openqa.selenium.Alert;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.By;
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

//https://stackoverflow.com/questions/35867102/how-to-work-with-selenium-chrome-driver-in-maven-without-chromedriver-exe
public class ScreenShotCaptureSelenium {
  // private int screenshotNum = 0;
   private WebDriver driver = null;
 
  // public ScreenShotCaptureSelenium(int screenshotNum, WebDriver driver) { // constructor
   public ScreenShotCaptureSelenium() { // constructor    
    //   this.driver = driver;
    //   this.screenshotNum = screenshotNum;
   }

  public boolean capture(String url) throws IOException {
  try{
       LOG.debug("entering capture() with url = " + url);
          driver = setUp();
          driver.get(url);
/*       LOG.debug("alert text = " + driver.switchTo().alert().getText());
     Alert alert = driver.switchTo().alert();
     alert.getText();
     alert.accept();
       LOG.debug("alert accepted = ");
  */     
       
          File screenshotFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
             LOG.debug("screenshotFile space = " + screenshotFile.getTotalSpace()); 
          LOG.debug("user.dir  = " + System.getProperty("user.dir")); // write into wildfly/bin !!
    /// not available in run      LOG.debug(" to be saved in " + Settings.getProperty("MAIL")); // Settings.getProperty("USER_DIR")
          File destinationFile = new File("selenium-"+ System.currentTimeMillis() + ".png");
           LOG.debug(" destinationFile " + destinationFile.getName());
          FileHandler.copy(screenshotFile, destinationFile);
      //      LOG.debug("Took Screenshot for " + site + " and saved as " + "site" + screenshotNum + ".png");
            LOG.debug("Screenshot saved to: " + destinationFile.getAbsolutePath());
            
            WebElement textBox = driver.findElement(By.name("my-text"));
             WebElement textBox2 = driver.findElement(By.id("my-text-id"));
   //         WebElement submitButton = driver.findElement(By.id("buttonTeeCreate"));
  //     submitButton.click(); 
 //      submitButton = driver.findElement(By.cssSelector("button"));
       
       textBox.sendKeys("Selenium");
       textBox2.sendKeys("Selenium22222");
  //     submitButton.click();
                        

            WebElement webElement = driver.findElement(By.id("body_PrimeFlex3"));
            MakeBorder(webElement);
            Thread.sleep(2000);
            
            
            File src = webElement.getScreenshotAs(OutputType.FILE);
            File destinationFile2 = new File("selenium-"+ LocalDateTime.now().format(ZDF_FILE) + ".png");
           LOG.debug(" destinationFile2 " + destinationFile2.getName());
           FileHandler.copy(src, destinationFile2);
     //      driver.switchTo().frame(iframeElement);
      // full screen scrolling needed   
         Screenshot aScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(driver);
         ImageIO.write(aScreenshot.getImage(), "PNG", new File("seleniumaFullScreen-"+ LocalDateTime.now().format(ZDF_FILE) + ".png"));
          LOG.debug("taken with Ashot() = C:/log/FullView.png "); // + destinationFile.getAbsolutePath());
  //     GraphicsEnvironment.isHeadless());

       
       
       
         LOG.debug("exiting capture()");
         return true;
 } catch ( UnhandledAlertException e) {   // UnexpectedAlertPresentException
    String msg = "UnhandledAlertException in capture " + e;
    LOG.info(msg);
    showMessageInfo(msg);
    Alert alert = driver.switchTo().alert();
  //  String alertText = alert.getText();
      LOG.debug("ERROR: (ALERT BOX DETECTED) - ALERT MSG : " + alert.getText());
    alert.accept();
      LOG.info("alert accepted");
    alert.dismiss(); //it is important you close alert or do something about it. 
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
//https://stackoverflow.com/questions/35867102/how-to-work-with-selenium-chrome-driver-in-maven-without-chromedriver-exe
public WebDriver setUp(){
try{
       LOG.debug("entering setUp()");
       
    //   if(driver == null){
    //       LOG.debug("driver == null");
    //   }
  // https://developer.chrome.com/docs/chromedriver/capabilities?hl=fr#:~:text=You%20can%20create%20an%20instance,to%2Fextension.crx%22))%3B  
//Setting up options to run our test script
   ChromeOptions options = new ChromeOptions();
 //  options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
   options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT); // important !!
   options.setCapability("browserName", "chrome");
   options.addArguments("start-maximized");  // Utiliser Chrome en mode plein écran
   options.addArguments("--start-maximized"); //  faut les -- ?
   options.addArguments("disable-extensions"); 
   options.setExperimentalOption("excludeSwitches",Arrays.asList("disable-popup-blocking")); // Bloquer les fenêtres de dialogue
   options.setExperimentalOption("useAutomationExtension", false); // how to hide the "Chrome is being controlled by automated software..." 
   options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
// new 18-05-2024
//   options.addArguments("disable-infobars");
   options.setPageLoadStrategy(PageLoadStrategy.NORMAL); // wait for all resources
///   options.addArguments("window-size=1920x1080");
 //  options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
   System.setProperty("java.awt.headless", "false"); // YES YES !!eviter erreur = java.awt.AWTException: headless environment 
  /*/Combining DesiredCapabilities with Options class: Use the merge(Capabilities) method of the Options class.
     DesiredCapabilities capabilities = new DesiredCapabilities();
     capabilities.setCapability(ChromeOptions.CAPABILITY, options);
     capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
     options.merge(capabilities);
 */    
    WebDriverManager.chromedriver().setup(); // using io.github.bonigarcia automatic webdriver search
    driver = new ChromeDriver(options);
    driver.manage().window().maximize();
    //https://www.browserstack.com/guide/wait-commands-in-selenium-webdriver
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));  // mod 04-06-2024 was 5
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
   return driver;
   }catch (Exception ex){
            String msg = "Exception in setUp() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
}  // end method setUp()   

// Function to Make border
   private void MakeBorder(WebElement Element){
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript(
            "arguments[0].style.border = '3px solid red'",
            Element);
    }

void main(){
  try{
    ScreenShotCaptureSelenium ex1 = new ScreenShotCaptureSelenium();
 //   ex1.capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/technical_info.xhtml");
    ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
    

 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in ExampleOne.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//

} // end Class

  /*     system lc = headless : why ?   
    System.setProperty("java.awt.headless", "false"); // eviter erreur = java.awt.AWTException: headless environment 
       
    Robot robot = new Robot(); 
       LOG.debug("line 00");
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
     LOG.debug("dimension = " + d.toString());
     
    Rectangle screenRect = new Rectangle(d);  // get screen size
       LOG.debug("line 01");
  //  BufferedImage capture = new Robot().createScreenCapture(screenRect);
    BufferedImage capture = robot.createScreenCapture(screenRect);
       LOG.debug("line 02");
    File imageFile = new File("c:/log/single-screen.bmp");
       LOG.debug("line 03");
    ImageIO.write(capture, "bmp", imageFile );
       LOG.debug("line 04");
    BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ImageIO.write(screenShot, "JPG", new File("c:/log/image.louis.jpg"));
     
  
       GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice(); // if multi-monitor configuration 
  
      //  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
     //   LOG.debug("screen size = " + size.toString());
        // width will store the width of the screen 
        double width = gd.getDisplayMode().getWidth(); 
        LOG.debug("width = " + width);
        double heigth = gd.getDisplayMode().getHeight(); 
        LOG.debug("heigth = " + heigth);
         this.driver.quit();
   */    /* 
   public void TakeRobotScreenshot() throws Exception {
 try{
       LOG.debug("entering capture robot()"); // with url = " + url);
    System.setProperty("java.awt.headless", "true"); // eviter erreur = java.awt.AWTException: headless environment 
    LOG.debug("is headless : " + GraphicsEnvironment.isHeadless());
    
    Robot robot = new Robot(); 
       LOG.debug("line 00");
    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());  // get screen size
       LOG.debug("line 01");
  //  BufferedImage capture = new Robot().createScreenCapture(screenRect);
    BufferedImage capture = robot.createScreenCapture(screenRect);
       LOG.debug("line 02");
    File imageFile = new File("c:/log/single-screen.bmp");
       LOG.debug("line 03");
    ImageIO.write(capture, "bmp", imageFile );
       LOG.debug("line 04");
    
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ImageIO.write(screenShot, "JPG", new File("c:/log/image.louis.jpg"));
  //  BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
  //      ImageIO.write(screenShot, "JPG", new File("d:\\"+formatter.format(now.getTime())+".jpg"));
   // assertTrue(imageFile .exists());
   LOG.debug("exiting robot()");

  }catch (Exception ex){
            String msg = "TakeRobotScreenshot " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
         //   return false;
  } 
   } // end method
 */  
      /*/ not used !! ol deprected solution
   public void initDriver() {
          System.setProperty("webdriver.chrome.driver", "C:/Program Files/Chrome Driver/chromedriver.exe"); // faut également copier manuelle chromedriver.exe
          this.driver = new ChromeDriver();
          this.driver.manage().window().setPosition(new Point(-2000, 0));
       //   String title = driver.getTitle();
          setUp();
     }
 */    