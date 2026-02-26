
package selenium;

import static interfaces.Log.LOG;
import java.io.IOException;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;


public class SeleniumTest{
  // private int screenshotNum = 0;
   private WebDriver driver = null;
 
  // public ScreenShotCaptureSelenium(int screenshotNum, WebDriver driver) { // constructor
    //   this.driver = driver;
    //   this.screenshotNum = screenshotNum;
  // }

 public boolean capture(String view) throws IOException {
  try{
      LOG.debug("entering capture with view1 = " + view);
      String url = utils.LCUtil.firstPartUrl()+ "/" + view;
    //  String url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + view;
         LOG.debug("entering capture with url v1 = " + url);
       driver = selenium.SeleniumController.setUp();
     //  driver.get("https://www.selenium.dev/selenium/web/web-form.html");
       driver.get(url);
          LOG.debug("current url = " + driver.getCurrentUrl());
          LOG.debug("Driver : page source = " + "\n" + driver.getPageSource()); // pour faire les recherches !
          LOG.debug("title = " + driver.getTitle());
          
          //driver.findElement(By.cssSelector("input[id='yesRadio']")).click();
          // Radio buttons are denoted by the <input> HTML tags having "type" as "radio"
         //  https://testsigma.com/blog/action-class-in-selenium/ 
         
  // explicit wait    https://www.guru99.com/implicit-explicit-waits-selenium.html    
         Actions actions = new Actions(driver); // il n'y a que Actions qui fonctionne !! click ne fonctionne pas !!
         
         WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
         wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form_club:ClubName")));
         
         Wait<WebDriver> wait2 = new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(30))
            .pollingEvery(Duration.ofSeconds(5))
            .ignoring(Exception.class);

    //     WebElement clickseleniumlink = wait2.until(new Function<WebDriver, WebElement>(){
//			public WebElement apply(WebDriver driver ) {
//			return driver.findElement(By.id("form_club:ClubName"));
  //       }
//	});
/*         
// club         
         WebElement element = driver.findElement(By.id("form_club:ClubName"));  // faut préfixe = id de form form_tee:select_gender
         actions.sendKeys(element, "Club de test via SeleniumL").perform(); // Ladies

         element = driver.findElement(By.id("form_club:ClubAddress"));
         actions.sendKeys(element, "Rue de l'Amazone 55").perform();

         element = driver.findElement(By.id("form_club:ClubCity"));
         actions.sendKeys(element, "B-1060 Bruxelles").perform();

         element = driver.findElement(By.id("form_club:ClubCountryAutoComplete"));
         actions.sendKeys(element, "be").sendKeys(Keys.ENTER); // yes yes

         element = driver.findElement(By.id("form_club:ClubWebsite"));
         actions.sendKeys(element, "www.fairmont.com").perform();

         element = driver.findElement(By.id("form_club:ButtonGPSCoordinates"));
         actions.click(element).perform();

         element = driver.findElement(By.id("form_club:ButtonCreateClub"));
         actions.click(element).perform();

          LOG.debug("line club created = done");
           Thread.sleep(15000);
           
           
 */
  // course         
          WebElement element = driver.findElement(By.id("form_course:CourseName"));
          actions.sendKeys(element, "Course de test selenium").perform();

          // à tester     https://www.scaler.com/topics/selenium-tutorial/how-to-handle-date-picker-in-selenium/  
          element = driver.findElement(By.id("form_course:picker_endDate"));
          actions.sendKeys(element,"07/07/2033 10:45").perform();  // Assuming the date format is MM/DD/YYYY HH:mm
          actions.click(); // click en dehors picker

          element = driver.findElement(By.id("form_course:ButtonCreateCourse"));
          actions.click(element).perform();
             LOG.debug("after click create course");
          Thread.sleep(15000); 
  // tee
         element = driver.findElement(By.id("form_tee:select_gender:1"));  // faut préfixe = id de form form_tee:select_gender
    // works also     element = driver.findElement(By.cssSelector("input[id='form_tee:select_gender:1']"));
         actions.sendKeys(element, "L").perform(); // Ladies

         element = driver.findElement(By.id("form_tee:select_start:2"));
         actions.sendKeys(element, "BLACK").perform();
//         LOG.debug("line 00c2");
         element = driver.findElement(By.id("form_tee:select_holes:1"));
         actions.sendKeys(element, "01-09").perform();
  //       LOG.debug("line 00c3");
         element = driver.findElement(By.id("form_tee:select_par:1"));
         actions.sendKeys(element, "71").perform();
//         LOG.debug("line 00c3");
         element = driver.findElement(By.id("form_tee:input_slope"));  
         actions.sendKeys(element, "129").perform();
//         LOG.debug("line 01a");
         element = driver.findElement(By.id("form_tee:input_rating"));
         actions.sendKeys(element, "70.5").perform();
  //         LOG.debug("line 01b");
         element = driver.findElement(By.id("form_tee:ButtonCreateTee"));
         actions.click(element).perform();
             LOG.debug("after click");
  /*       
        List<WebElement> radioBtns = driver.findElements(By.name("form_tee:select_start"));
        for (WebElement radioBtn : radioBtns) {
            if (radioBtn.getAttribute("value").equals("BLACK")) {
                radioBtn.click();
                break;
            }
        } //end for
         
          LOG.debug("after List");
     */    
         
         
       // les msg erreur apparaissent !
        Thread.sleep(5000);
  // no      ((JavascriptExecutor) driver).executeScript("alert('hello world');");
           LOG.debug("after wait");
 // https://www.softwaretestinghelp.com/radio-buttons-in-selenium/
 
 /*
       List radioButton = driver.findElements(By.name("colour"));
 
           // selecting the Radio buttons by Name
             int Size = radioButton.size();                 // finding the number of Radio buttons
             for(int i=0; i < Size; i++)                       // starts the loop from first Radio button to the last one
          {    
         String val = radioButton.get(i).getAttribute("value");
    // Radio button name stored to the string variable, using 'Value' attribute
          if (val.equalsIgnoreCase("Green"))   // equalsIgnoreCase is ignore case(upper/lower)
                 {                   // selecting the Radio button if its value is same as that we are looking for
        radioButton.get(i).click();
        break;
            }
              }
    */
         return true;
 } catch ( UnhandledAlertException e) {   // UnexpectedAlertPresentException
    String msg = "UnhandledAlertException in capture " + e;
    LOG.info(msg);
    showMessageInfo(msg);
 ///   Alert alert = driver.switchTo().alert();
///      LOG.debug("ERROR: (ALERT BOX DETECTED) - ALERT MSG : " + alert.getText());
 ///   alert.accept();
      LOG.info("alert accepted");
 ///   alert.dismiss(); //it is important you close alert or do something about it. 
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
  
  }finally{ 
     driver.quit();
  }
  }
 // ici main !!
 public static void main(String args[]){
  try{
   // SeleniumTest ex1 = new SeleniumTest();
  //  ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
 //   boolean b = new ExampleOne().capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/tee.xhtml");
     boolean b = new SeleniumTest().capture("course.xhtml");
   LOG.debug("result in main = " + b) ;
    
 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in ExampleOne.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//
} // end Class