package entite;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import utils.LCUtil;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;

/**
 * Settings de l'application — chargés depuis golflc_settings.properties
 * ✅ @ApplicationScoped — singleton CDI
 * ✅ @PostConstruct — init automatique par WildFly
 * ✅ Constructeur public — requis par CDI
 * ✅ Champ d'instance — plus static (singleton CDI suffit)
 */
@Named("settings")
@ApplicationScoped
public class Settings implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Champ d'instance — @ApplicationScoped garantit le singleton
    private Map<String, String> settings = new HashMap<>();

    // ✅ Constructeur public — requis par CDI
    public Settings() { }

    // ========================================
    // INITIALISATION — appelée automatiquement par CDI
    // ========================================

    /**
     * Initialisation des settings depuis golflc_settings.properties
     * et les variables d'environnement système.
     */
    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Properties properties = new Properties();

            try (InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("golflc_settings.properties")) {

                if (inputStream == null) {
                    throw new RuntimeException("golflc_settings.properties introuvable !");
                }
                properties.load(inputStream);
            }

            LCUtil.printProperties("golflc_settings.properties");

            // Variables système
            String userHome = System.getProperty("user.home");
            LOG.debug(methodName + " - user.home = " + userHome);

            String userApp = properties.getProperty("settings.userapp", "");
            LOG.debug(methodName + " - USER_APPLICATION = " + userApp);

            String userDir   = userHome + userApp;
            String webapp    = userDir  + "/src/main/webapp/";
            String resources = webapp   + "resources/";

            LOG.debug(methodName + " - env.MAVEN_PROJECTBASEDIR = "
                    + System.getenv("env.MAVEN_PROJECTBASEDIR"));

            // ✅ Construction de la map
            settings.clear();
            settings.put("EXECUTION",          properties.getProperty("settings.execution", ""));
            settings.put("USER_HOME",           userHome);
            settings.put("SMTP_PASSWORD",       System.getenv("SMTP_PASSWORD"));
            settings.put("SMTP_SERVER",         System.getenv("SMTP_SERVER"));
            settings.put("SMTP_USERNAME",       System.getenv("SMTP_USERNAME"));
            settings.put("BATCH",               userDir   + "/InputBatchFiles/");
            settings.put("WEBAPP",              webapp);
            settings.put("RESOURCES",           resources);
            settings.put("IMAGES_LIBRARY",      resources + "images/");
            settings.put("PHOTOS_LIBRARY",      resources + "images/photos/");
            settings.put("THUMBNAILS_LIBRARY",  resources + "images/thumbnails/");
            settings.put("WHS_CALCULATIONS",    resources + "calculations/");
            settings.put("TARGET",              userDir   + "/target");
            settings.put("GOOGLE_MAPS_API_KEY", System.getenv("GOOGLE_MAPS_API_KEY"));

            LOG.debug(methodName + " - Settings initialized:");
            settings.forEach((k, v) -> LOG.debug(TAB + k + " = " + v));

        } catch (Exception e) {
            String msg = "Fatal Exception in " + methodName + " : " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
        }
    } // end method

    // ========================================
    // ACCÈS AUX PROPRIÉTÉS
    // ========================================

    /**
     * Retourne une propriété par clé
     * ✅ Méthode d'instance — plus static
     */
    public String getProperty(String key) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        String value = settings.get(key);
        if (value == null) {
            LOG.warn(methodName + " - key not found: " + key);
        }
        return value;
    } // end method

    // ========================================
    // TO STRING
    // ========================================

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(NEW_LINE).append("FROM ENTITE : ")
              .append(getClass().getSimpleName().toUpperCase())
              .append(NEW_LINE);
            settings.forEach((k, v) ->
                    sb.append(TAB).append(k).append(" = ").append(v).append(NEW_LINE));
            return sb.toString();
        } catch (Exception e) {
            String msg = "Exception in Settings.toString = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return msg;
        }
    } // end method

} // end class

/*
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
import java.util.HashMap;
import utils.LCUtil;
import static org.omnifaces.util.Faces.getResourceAsStream;

@Named("settings")
@ApplicationScoped

public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    // Map statique pour partager les settings dans toute l'application
    private static Map<String, String> settings = new HashMap<>();

    // Constructeur privé pour empêcher instanciation
    private Settings() { }

    /**
     * Initialisation des settings depuis le fichier golflc_settings.properties
     * et les variables système / environnement.
     
    public static void init() {
        try {
            LOG.debug("Entering Settings.init()");
            Properties properties = new Properties();
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("golflc_settings.properties")) {
                if (inputStream == null) {
                    throw new RuntimeException("Fichier golflc_settings.properties introuvable !");
                }
                properties.load(inputStream);
            }

            LCUtil.printProperties("golflc_settings.properties");

            // Variables système
            String USER_HOME = System.getProperty("user.home");
            LOG.debug("System property user.home = " + USER_HOME);

            String USER_APP = properties.getProperty("settings.userapp", "");
            LOG.debug("USER_APPLICATION = " + USER_APP);

            String USER_DIR = USER_HOME + USER_APP;
            String WEBAPP = USER_DIR + "/src/main/webapp/";
            String RESOURCES = WEBAPP + "resources/";

            LOG.debug("env.MAVEN_PROJECTBASEDIR = " + System.getenv("env.MAVEN_PROJECTBASEDIR"));
            
            
            // Construction de la map (mutable)
            settings.clear();
            settings.put("EXECUTION", properties.getProperty("settings.execution", ""));
            settings.put("USER_HOME", USER_HOME);
         //   settings.put("MAIL", System.getenv("SMTP_PASSWORD")); // ou fallback si null
            settings.put("SMTP_PASSWORD", System.getenv("SMTP_PASSWORD")); // ou fallback si null
            settings.put("SMTP_SERVER", System.getenv("SMTP_SERVER")); // ou fallback si null
            settings.put("SMTP_USERNAME", System.getenv("SMTP_USERNAME")); // ou fallback si null
            settings.put("BATCH", USER_DIR + "/InputBatchFiles/");
            settings.put("WEBAPP", WEBAPP);
            settings.put("IMAGES_LIBRARY", RESOURCES + "images/");
            settings.put("PHOTOS_LIBRARY", RESOURCES + "images/photos/");
            settings.put("RESOURCES", RESOURCES);
            settings.put("WHS_CALCULATIONS", RESOURCES + "calculations/");
            settings.put("THUMBNAILS_LIBRARY", RESOURCES + "images/thumbnails/");
            settings.put("TARGET", USER_DIR + "/target");

            // Log des settings
            LOG.debug("Settings initialized:");
            settings.forEach((k, v) -> LOG.debug(k + " = " + v));

        } catch (Exception e) {
            String msg = "Fatal Exception in Settings.init(): " + e;
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
        }
    }

    /**
     * Retourne une propriété par clé
     
    public static String getProperty(String key) {
        return settings.get(key);
    }

    /**
     * Affichage complet des settings pour debug
     
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Settings:\n");
        settings.forEach((k, v) -> sb.append(TAB).append(k).append(" = ").append(v).append(NEW_LINE));
        return sb.toString();
    }
}
*/
/*
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

     @Override
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
 
 
 
 
    }catch(Exception e){
        String msg = "£££ Exception in Settings.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} */ 

