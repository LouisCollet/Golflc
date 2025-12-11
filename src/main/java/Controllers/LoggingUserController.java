package Controllers;

import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import utils.LCUtil;
import static utils.LCUtil.showMessageInfo;

public class LoggingUserController {
    private static String text = "start text";

 public static void write(String text){
   //  <p style="font-size:14px; "> Any text whose font we want to change </p>
   // mod 21-04-2025
    writeText(
           // "<br/>" +
             "<p" 
            + "style='fontsize:1.5em;color:black;>'"
            + text
            + "</p>");
 }

 public static void write(String text, String param){
     if(param.equalsIgnoreCase("b")){
         text = "<b>" + text + "</b>";
     }
     if(param.equalsIgnoreCase("i")){
       //  text = "<br/>" + "<i>" + text + "</i>";
         text = "<i>" + text + "</i>"; // mod 21-09-2024
     }
     if(param.equalsIgnoreCase("u")){
         text = "<u>" + text + "</u>";
     }
     if(param.equalsIgnoreCase("t")){  // titre
         text = "<p>" + "<h1>" + "<b>" + text.toUpperCase() + "</b>" + "</h1>" + "</p>";
     }
     if(param.equalsIgnoreCase("c")){  // color
         text = "<h1>" + "<p style='color:red;'>" +  "<b>" + text.toUpperCase() + "</b>" + "</h1>" + "</p>";
     }
      writeText("<br/>" + text);
 }   

 
  public static void writeText(String newText){
    text = text + newText;
 
  }
  /*
 public static void writeToFile(String text){
   //  Path path = null;
 //try{
 //               path = Paths.get(Settings.getProperty("RESOURCES") + "calculations/", FILE_NAME);
// }catch (java.lang.NoClassDefFoundError e){ // no JSF Session test with RUN
        //       LOG.error(msg);
 //              path = Paths.get("c:/log", FILE_NAME);
        //       LOG.debug("saved path = " + path);
//            }
 try (BufferedWriter writer = Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND)) {
        writer.write(text);
 //       writer.flush(); // bien utile 
//        LOG.debug("success write!! " + text);
    } catch (IOException e) {
        LOG.error("exception in writeTextFile !!" + e);
    }
}
 */

  public boolean createUpdateLoggingUser(LoggingUser logging) {
  try{
      boolean b = new Controllers.MongoCalculationsController().create(logging);
      return false;
  } catch (Exception e) {
        String msg = "exception in read !!" + e + "No calculations available !";
        LOG.info(msg);
        write(msg);
        showMessageInfo(msg);
        return false;
    }
}
    /*
  public boolean old_createUpdateLoggingUser(LoggingUser logging, Connection conn) {
  try{
//     Path path = Paths.get(Settings.getProperty("RESOURCES") + "calculations/", FILE_NAME);
    //return Files.readString(path, StandardCharsets.UTF_8);
       logging.setLoggingCalculations(text);
       LOG.debug("entering createUpdateLoggingUser with logging = " + logging);
 //      LOG.debug("entering createUpdateLoggingUser with text = " + text);
  //  var v = new read.ReadLoggingUser().read(logging, conn);
    int v = new find.FindLoggingUser().find(logging, conn);
       LOG.debug("result readLoggingUser = " + v);
 //   logging.setLoggingCalculations(text);
    if(v == 0){
          String msg = LCUtil.prepareMessageBean("logging.notfound");
          LOG.debug(msg);
           if(new create.CreateLoggingUser().create(logging, conn)){
               LOG.debug("create LoggingUser OK " + v);
               return true;
           }
 //         LCUtil.showMessageInfo(msg);
     }else{
          String msg = LCUtil.prepareMessageBean("logging.found")+ logging;
          LOG.debug(msg);
          if(new update.UpdateLoggingUser().update(logging, conn)){
              LOG.debug("Updte LoggingUser OK" + v);
              return true;
          }
 //         LCUtil.showMessageInfo(msg);
     }
  return false;
  } catch (Exception e) {
        String msg = "exception in read !!" + e + "No calculations available !";
        LOG.info(msg);
        write(msg);
        showMessageInfo(msg);
        return false;
    }
}
  */
public String read(LoggingUser logging, Connection conn) {
  try{
    return new read.ReadLoggingUser().read(logging, conn).getLoggingCalculations();
  } catch (Exception e) {
        String msg = "exception in read !!" + e + "No calculations available !";
        LOG.info(msg);
        write(msg);
        showMessageInfo(msg);
        return null;
    }
}
    public static String getText() {
        return text;
    }

    public static void setText(String text) {
        LoggingUserController.text = text;
    }
 void main() throws SQLException, Exception{
 //public static void main(String[] args){
    LOG.debug("line 0");
// ne fonctionne pas !!!
//       setFILE_NAME("temp3.txt");
       LOG.debug("line 1");
  //     write("19-08-2021 from method Welcome to Java 8" + NEW_LINE + " again louis " 
  //             + System.lineSeparator() + " after lineSeparator");
//       String s = read();
  //     LOG.debug("string readed = " + System.lineSeparator() + s);
} //end main
} // end class