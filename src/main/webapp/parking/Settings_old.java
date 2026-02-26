package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import utils.LCUtil;
import static org.omnifaces.util.Faces.getResourceAsStream;

@Named("settings")
@ApplicationScoped
public class Settings implements Serializable{
    private static final long serialVersionUID = 1L;
    private static Map<String, String> settings;

   public Settings(){    }

 public static void init(){   
  try{
         LOG.debug("entering init Settings");
       Properties properties = new Properties();
   try ( InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("golflc_settings.properties")) {  //mod 15-12-2025
       //  InputStream inputStream = getResourceAsStream("golflc_settings.properties")){
       if (inputStream == null) {  // new 31-12-2025
          throw new RuntimeException("Fichier golflc_settings.properties introuvable !");
       }
         properties.load(inputStream);
      }
      
       utils.LCUtil.printProperties("golflc_settings.properties");
   //    String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD"); // new 23-11-2025 comme dans python
       
       String USER_HOME = System.getProperty("user.home"); // C:\Users\Louis Collet
          LOG.debug("System property user.home = " + USER_HOME);
       String USER_APP = properties.getProperty("settings.userapp");
          LOG.debug("USER APPLICATION = " + USER_APP);
       String USER_DIR = USER_HOME + USER_APP;
       String WEBAPP = USER_DIR + "/src/main/webapp/";
       String RESOURCES = WEBAPP + "resources/"; 
       settings = Map.ofEntries(
          new AbstractMap.SimpleEntry<>("EXECUTION", properties.getProperty("settings.execution")),
          new AbstractMap.SimpleEntry<>("USER_HOME", USER_HOME),
          //new AbstractMap.SimpleEntry<>("MAIL", properties.getProperty("settings.mail")),
          new AbstractMap.SimpleEntry<>("MAIL", System.getenv("SMTP_PASSWORD")),  // new 23-11-2025, mod 10-12-2025
          new AbstractMap.SimpleEntry<>("BATCH", USER_DIR + "/InputBatchFiles/"),
          new AbstractMap.SimpleEntry<>("WEBAPP", WEBAPP),
          new AbstractMap.SimpleEntry<>("IMAGES_LIBRARY", RESOURCES + "images/"),
          new AbstractMap.SimpleEntry<>("PHOTOS_LIBRARY", RESOURCES + "images/photos/"),
    //      new AbstractMap.SimpleEntry<>("HELP", WEBAPP + "help/"),  //deleted 04-08-2023 transfer to mongodb
          new AbstractMap.SimpleEntry<>("RESOURCES", RESOURCES),
          new AbstractMap.SimpleEntry<>("WHS_CALCULATIONS", RESOURCES + "calculations/"),
          new AbstractMap.SimpleEntry<>("THUMBNAILS_LIBRARY", RESOURCES + "images/thumbnails/"),
          new AbstractMap.SimpleEntry<>("TARGET", USER_DIR + "/target")
          );
          LOG.debug("Properties Settings \n");
       settings.forEach((k, v) -> LOG.debug(("settings = " + k + " : " + v)));
 //      LOG.debug("exiting from Settings ..." );
   }catch (Exception e){
	String msg = "Fatal Exception in init() Settings : "  + e;
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
}   
} //end method

    public static String getProperty(String s) {
        return settings.get(s);
    }

/*     @Override
public String toString(){
 try{ 
 return 
         NEW_LINE + "FROM ENTITE : Settings to be modified "
         
          //     WEBAPP + "help/"
  //             + "HELP " + map.get("HELP")
//               + " ,USER_APP : " + getUSER_APP()
          + NEW_LINE + TAB
     //          + " ,WEBAPP : " + getWEBAPP()
  //             + " ,HELP : " + HELP
            + NEW_LINE + TAB
  //             + " ,IMAGES_LIBRARY : " + getIMAGES_LIBRARY()
//               + " ,PHOTOS_LIBRARY : " + getPHOTOS_LIBRARY()
            + NEW_LINE + TAB
//               + " ,THUMBNAILS_LIBRARY : " + getTHUMBNAILS_LIBRARY()
 //              + " execution : " + EXECUTION
         ;
*/
 @Override // new 31-12-2025 remplace versio au-dessus
public String toString() {
    StringBuilder sb = new StringBuilder("Settings:\n");
    settings.forEach((k,v) -> sb.append(k).append("=").append(v).append("\n"));
    return sb.toString();
}
 
 
 
    }catch(Exception e){
        String msg = "£££ Exception in Settings.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}  
} //end class