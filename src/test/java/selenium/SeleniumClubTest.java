package selenium;

import Controllers.SeleniumController;
import static interfaces.Log.LOG;
import jakarta.faces.annotation.ApplicationMap;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
//import org.openqa.selenium.support.ui.Select;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.BeforeTest;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

//https://www.browserstack.com/guide/testng-framework-with-selenium-automation
public class SeleniumClubTest{
   private static WebDriver driver = null; // mod 27-03-2025
// yes yes https://naveenautomationlabs.com/2-how-to-create-testng-class-with-different-annotations/
   
//@BeforeClass
public static void beforeClassMethod() {
   LOG.debug("entering Before Class method");
}

//@BeforeTest
public void beforeMethod() {
   LOG.debug("entering Before Method");
}

//@Test
//public void test() {
//LOG.debug("Inside Test method");
//}

//@AfterTest
public void afterMethod() {
   LOG.debug("entering After method");
}

//@AfterClass
public static void afterClassMethod() {
LOG.debug("entering After Class method");
}
//@BeforeMethod
public void openBrowser(){
// driver.get("https://www.browserstack.com/");
// driver.findElement(By.id("signupModalButton")).click();
   LOG.debug("We are currently on the following URL" + driver.getCurrentUrl());
}

 //@Test(description="ce que fait ce test") //, enabled="true")
 //@Test(dependsOnMethod=”Login”)
 //@Test(priority=1)

@Inject @ApplicationMap
private Map<String, Object> applicationMap;


 public boolean testing(String view){ // throws IOException { // static added 27-03-2025
  try{
      LOG.debug("entering testing for view = " + view);
     // LOG.debug("applicationMap clubcreated = " + applicationMap.get("clubCreated"));

         driver = Controllers.SeleniumController.setUp();
      //   driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
     //    driver.manage().window().maximize();             

         String firstPartUrl = utils.LCUtil.firstPartUrl() + "/";
            LOG.debug("firstPartUrl = " + firstPartUrl);
 //1. club
         String url = firstPartUrl + view;
         LOG.debug("url = " + url);
       //   String url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + view;   // run testing
         driver.get(url);
            LOG.debug("current url = " + driver.getCurrentUrl());
    //      LOG.debug("Driver : page source = " + "\n" + driver.getPageSource()); // pour faire les recherches !
            LOG.debug("view  title = " + driver.getTitle());
         Actions actions = new Actions(driver); // il n'y a que Actions qui fonctionne !! click ne fonctionne pas toujours!!

         WebElement element = driver.findElement(By.id("form_club:ClubName"));  // faut préfixe = id de form form_tee:select_gender
         actions.sendKeys(element, "Club de test via SeleniumL").perform();

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
  //    element = driver.findElement(By.id("aspnetForm:onetrust-accept-btn-handler"));
  //    actions.click(element).perform();
         
         element = driver.findElement(By.id("form_club:ButtonGPSCoordinates"));
         actions.click(element).perform();
// 2e exec : was lost à corriger mod 24-06-2025
         country = driver.findElement(By.id("form_club:ClubCountry"));
         actions.sendKeys(country, "be").sendKeys(Keys.ENTER);

         element = driver.findElement(By.id("form_club:ButtonCreateClub"));
         actions.click(element).perform();

          LOG.debug("club created !");
        
   //       Thread.sleep(5000);
  // 2. course 
  
        driver.get(firstPartUrl + "course.xhtml");
             LOG.debug("current url = " + driver.getCurrentUrl());
             LOG.debug("view title = " + driver.getTitle());
         WebElement club_id = driver.findElement(By.id("form_course:clubId"));
            LOG.debug("club_id created = " + club_id.toString());
            LOG.debug("club_id getText = " + club_id.getText());  // Retrieve the text from the input field     
            LOG.debug("club_id getText = " + club_id.getDomAttribute("club_id"));  // Retrieve the text from the input field     
             
             
             
        element = driver.findElement(By.id("form_course:CourseName"));
        actions.sendKeys(element, "Course de test selenium").perform();

          // https://www.scaler.com/topics/selenium-tutorial/how-to-handle-date-picker-in-selenium/  
        element = driver.findElement(By.id("form_course:picker_endDate"));
        actions.sendKeys(element,"07/07/2033 10:45").perform();  // Assuming the date format is MM/DD/YYYY HH:mm, faut readOnly="false" dans p:datePicker
        actions.click(); // click en dehors picker !!

        element = driver.findElement(By.id("form_course:ButtonCreateCourse"));
        actions.click(element).perform();
             LOG.debug("course created !");
      //    Thread.sleep(5000); 
  // 3. tee
        driver.get(firstPartUrl + "tee.xhtml");
          LOG.debug("current url = " + driver.getCurrentUrl());
          LOG.debug("view title = " + driver.getTitle());
   //     Select objSelect = new Select(driver.findElement(By.id("form_tee:select_gent")));
  //      objSelect.selectByIndex(1);  
          
        element = driver.findElement(By.id("form_tee:select_gent"));  // dans tee.xhtml =  p:selectOneButton id="select_gender"
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
             
         SeleniumController.tearDown(driver);
             
  /*       //      Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
   //      wait.until((WebDriver d) -> country.isDisplayed());
             // works also     element = driver.findElement(By.cssSelector("input[id='form_tee:select_gender:1']"));  
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
     //   Thread.sleep(5000);
  // no      ((JavascriptExecutor) driver).executeScript("alert('hello world');");
       //    LOG.debug("after wait");
 // https://www.softwaretestinghelp.com/radio-buttons-in-selenium/
 
 /*          //driver.findElement(By.cssSelector("input[id='yesRadio']")).click();
          // Radio buttons are denoted by the <input> HTML tags having "type" as "radio"
         //  https://testsigma.com/blog/action-class-in-selenium/ 
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
 //@AfterMethod
public void postSignUp(){
   LOG.debug("postSignUp = " + driver.getCurrentUrl());
}

 //@AfterClass
public void afterClass(){
    LOG.debug("afterClass + driver.quit()");
    driver.quit();
}
 
 public static void main(String args[]){
  try{
 //  SeleniumClub ex1 = new SeleniumClub();
  //  ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
 //   boolean b = new ExampleOne().capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/tee.xhtml");
     boolean b = new SeleniumClubTest().testing("club.xhtml");
   LOG.debug("result SeleniumClubTest.main = " + b) ;
    
 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Exception in SeleniumClubTest.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//
} // end Class