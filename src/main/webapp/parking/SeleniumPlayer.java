
package selenium;

import static interfaces.Log.LOG;
import java.io.IOException;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class SeleniumPlayer{
   private WebDriver driver = null;

 public boolean capture(String view) throws IOException {
  try{
      LOG.debug("entering capture with view1 = " + view);
      String url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + view;
         LOG.debug("entering capture with url v1 = " + url);
        driver = selenium.SeleniumController.setUp();
     //  driver.get("https://www.selenium.dev/selenium/web/web-form.html");
        driver.get(url);
          LOG.debug("current url = " + driver.getCurrentUrl());
    //      LOG.debug("Driver : page source = " + "\n" + driver.getPageSource()); // pour faire les recherches !
          LOG.debug("title = " + driver.getTitle());
          LOG.debug("window handle = " + driver.getWindowHandle());  //sert à circuler entre url
        Actions actions = new Actions(driver); // il n'y a que Actions qui fonctionne !! click ne fonctionne pas toujours!!
/// player
        WebElement element = driver.findElement(By.id("form_player:PlayerId"));  // faut préfixe = id de form form_tee:select_gender
        actions.sendKeys(element, "123456").perform();
//        Thread.sleep(3000);
        element = driver.findElement(By.id("form_player:wizardPlayer_next"));
        actions.click(element).perform();

  /// new tab
        element = driver.findElement(By.id("form_player:PlayerFirstName"));
        actions.sendKeys(element, "Collet First Name").perform();

        element = driver.findElement(By.id("form_player:PlayerLastName"));
        actions.sendKeys(element, "Collet Last Name").perform();
      
        element = driver.findElement(By.id("form_player:PlayerBirthDate"));
        actions.sendKeys(element,"25-04-1950").perform();  // Assuming the date format is MM/DD/YYYY HH:mm, faut readOnly="false" dans p:datePicker
   //     actions.click(); // click en dehors picker !!
      
        element = driver.findElement(By.id("form_player:PlayerGender:1"));
        actions.sendKeys(element, "L").perform(); // Ladies
        
//  LOG.debug("before language!");
        element = driver.findElement(By.id("form_player:PlayerLanguage"));  // dans .xhtml c'est <p:selectOneMenu id="PlayerLanguage"
        actions.click(element).perform(); // click on ?? Select language ??
        Actions builder = new Actions(driver);
        Action languageChoice = builder
                .moveToElement(driver.findElement(By.id("form_player:PlayerLanguage_3"))) // FRANCAIS
                .click()
                .build();
        languageChoice.perform(); 
   LOG.debug("after language!");
   
    List<WebElement> list = driver.findElements(By.id("form_player:PlayerLanguage"));
          LOG.debug("list size() = " + list.size());
   WebElement e = driver.findElement(By.xpath("//*[text()='GERMAN']"));
   LOG.debug("Element with text(): " + e.getText() );
   
   
   
        Thread.sleep(1000);
        element = driver.findElement(By.id("form_player:wizardPlayer_next"));
        actions.click(element).perform();
       
  // new tab
         element = driver.findElement(By.id("form_player:PlayerStreet"));
         actions.sendKeys(element, "Rue de l'Amazone 55").perform();
 //LOG.debug("after street !");
         element = driver.findElement(By.id("form_player:PlayerZipcode"));
         actions.sendKeys(element, "B-1060").perform();
 //LOG.debug("after zip !");
         element = driver.findElement(By.id("form_player:PlayerCity"));
         actions.sendKeys(element, "Bruxelles").perform();
 //LOG.debug("after city !");        
         WebElement country = driver.findElement(By.id("form_player:PlayerCountry"));
         actions.sendKeys(country, "be").sendKeys(Keys.ENTER); // faut forceSelection="false" dans autocomplete du .xhtml
 //LOG.debug("after country !");
         element = driver.findElement(By.id("form_player:PlayerEmailOne"));
         actions.sendKeys(element, "www.fairmont@com.be").perform();
 //LOG.debug("after emailone !");        
         element = driver.findElement(By.id("form_player:PlayerEmailTwo"));
         actions.sendKeys(element, "www.fairmont@com.be").perform();
  //LOG.debug("after emailtwo !");       
         element = driver.findElement(By.id("form_player:ButtonCompletePlayerAddress"));
         actions.click(element).perform();
         
         Thread.sleep(1000);
         element = driver.findElement(By.id("form_player:wizardPlayer_next"));
         actions.click(element).perform();
          LOG.debug("after complete address");
        //  Thread.sleep(5000);
  // new tab 
  // LOG.debug("before PlayerHandicap");        
          Thread.sleep(500); // à remplacer par mieux !!
          element = driver.findElement(By.id("form_player:PlayerHandicap"));
 //         LOG.debug("line 2");
          actions.sendKeys(element, "27.9").perform();
 //  LOG.debug("before PlayerHandicapDate");
          element = driver.findElement(By.id("form_player:PlayerHandicapDate"));
          actions.sendKeys(element,"07-06-2023 10:45").perform();  // Assuming the date format is MM-DD-YYYY HH:mm, faut readOnly="false" dans p:datePicker
     //     actions.click(); // click en dehors picker !!
   LOG.debug("before home club !");
   // new 
   
  
    String parentWindow = driver.getWindowHandle();
    LOG.debug("parent window player.xhtml= " + parentWindow);
   
    element = driver.findElement(By.id("form_player:ButtonHomeClub")); // va vers dialogCub.xhtml
         actions.click(element).perform();
          Thread.sleep(500); // à remplacer par mieux !!
/*
  ///        url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + "dialogClub.xhtml";
  ///        driver.get(url);
   ///      LOG.debug("child url = dialogClub ?? " + driver.getCurrentUrl());
    ///     LOG.debug("title = " + driver.getTitle());
     //    LOG.debug("window dialog handle ?? = " + driver.getWindowHandle());  //sert à circuler entre url
        
   // Opens a new window and switches to new window
         driver.switchTo().newWindow(WindowType.WINDOW);
         
   //      String originalWindow = driver.getWindowHandle();
   //      LOG.debug("original window = " + originalWindow);
//Check we don't have other windows open already
    //     assert driver.getWindowHandles().size() == 1;
         LOG.debug("Window handles size = " + driver.getWindowHandles().size());
//Click the link which opens in a new window
     //    driver.findElement(By.linkText("new window")).click();

//Wait for the new window or tab
     //    wait.until(numberOfWindowsToBe(2));
   Thread.sleep(1500); // à remplacer par mieux !!
//Loop through until we find a new window handle
for (String windowHandle : driver.getWindowHandles()) {
    if(!parentWindow.contentEquals(windowHandle)) {
        driver.switchTo().window(windowHandle);
        LOG.debug("switched to " + windowHandle);
        break;
    }
}
   
Set<String> allWindowHandles = driver.getWindowHandles();
for(String handle : allWindowHandles){
    LOG.debug("Window handle - > " + handle);
}
 */ 
 Thread.sleep(5500); // à remplacer par mieux !!
LOG.debug("handle switched to dialogClub.xhtml" + driver.getWindowHandle());
 LOG.debug("current url = " + driver.getCurrentUrl());
          LOG.debug("title = " + driver.getTitle());
     url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + "dialogClub.xhtml";
         driver.get(url);     
     LOG.debug("current url = " + driver.getCurrentUrl());
          LOG.debug("title = " + driver.getTitle());     
          
   element = driver.findElement(By.id("form_select_club:clubs:3:ButtonSelectClub"));
   actions.click(element).perform();

// WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
//Wait for the new tab to finish loading content
   //  wait.until(titleIs("Select Dialog Club Page"));
        
         LOG.debug("after wait");
         Thread.sleep(2000);
         element = driver.findElement(By.id("form_select_club:clubs:3:ButtonSelectClub"));
         actions.click(element).perform();
         // faut revenir à la page de départ !!
         //Switch back to the old tab or window
         
  //    driver.switchTo().window(parentWindow);
// works
///          element = driver.findElement(By.id("form_player:PlayerHomeClub"));
      //   element.clear();
///          actions.sendKeys(element, "103").perform();
          actions.click();
        //  actions.click(element).perform();
   LOG.debug("after home club !");
          Thread.sleep(1000);
    //      driver.switchTo().window(parentWindow);
          LOG.debug("after home club 1!");
          Thread.sleep(1000);
          
           url = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/" + "player.xhtml";
         driver.get(url);     
     LOG.debug("current url = " + driver.getCurrentUrl()); // error retourne au premier tab playerId !!
          LOG.debug("title = " + driver.getTitle());    
     //     actions.click(element).perform(); // ne fonctionne pas !!
          element = driver.findElement(By.id("form_player:wizardPlayer_next"));
          LOG.debug("after home club 2!");
          actions.click(element).perform();
          LOG.debug("after home club 3 !");
    // final tab
          Thread.sleep(20000);
          return true;
  /*           
          
          List<WebElement> list = driver.findElements(By.className("_Rm"));

          puis utiliser liste !!
          element = driver.findElement(By.id("form_course:CourseName"));
          actions.sendKeys(element, "Course de test selenium").perform();

          // https://www.scaler.com/topics/selenium-tutorial/how-to-handle-date-picker-in-selenium/  
          element = driver.findElement(By.id("form_course:picker_endDate"));
          actions.sendKeys(element,"07/07/2033 10:45").perform();  // Assuming the date format is MM/DD/YYYY HH:mm, faut readOnly="false" dans p:datePicker
          actions.click(); // click en dehors picker !!

          element = driver.findElement(By.id("form_course:ButtonCreateCourse"));
          actions.click(element).perform();
             LOG.debug("course created !");
          Thread.sleep(5000); 
           
  // tee
         LOG.debug("current url = " + driver.getCurrentUrl());
         LOG.debug("title = " + driver.getTitle());
         element = driver.findElement(By.id("form_tee:select_gender:1"));  // faut préfixe = id de form form_tee:select_gender
         actions.sendKeys(element, "L").perform(); // Ladies

         element = driver.findElement(By.id("form_tee:select_start:2"));
         actions.sendKeys(element, "BLACK").perform();

         element = driver.findElement(By.id("form_tee:select_holes:1"));
         actions.sendKeys(element, "01-09").perform();
 
         element = driver.findElement(By.id("form_tee:select_par:1"));
         actions.sendKeys(element, "71").perform();

         element = driver.findElement(By.id("form_tee:input_slope"));  
         actions.sendKeys(element, "129").perform();

         element = driver.findElement(By.id("form_tee:input_rating"));
         actions.sendKeys(element, "70.5").perform();

         element = driver.findElement(By.id("form_tee:ButtonCreateTee"));
         actions.click(element).perform();
             LOG.debug("tee created !");
         Thread.sleep(5000);
             
             
   */          
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
                 
        
     //   actions.click(element).perform();
 ///       actions.sendKeys(Keys.ARROW_UP).perform();
  ///      actions.sendKeys(Keys.ARROW_UP).perform();
  ///      actions.click(element).perform();
 ///       LOG.debug("line 02 !");
        
     //   Select selectLanguage = new Select(element);
  //      List <WebElement> elementCount = selectLanguage.getOptions();
  //      LOG.debug("elementCount = " + elementCount.size());
       // selectLanguage.selectByVisibleText("??");
        
         
       // les msg erreur apparaissent !
 
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
    SeleniumPlayer sel = new SeleniumPlayer();
  //  ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
 //   boolean b = new ExampleOne().capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/tee.xhtml");
     boolean b = sel.capture("player.xhtml");
   LOG.debug("result seleniumPlayer in main = " + b) ;
    
 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in SeleniumPlayer.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//
} // end Class