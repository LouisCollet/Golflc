
package settings;

import static interfaces.Log.LOG;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import utils.LCUtil;

/**
 * Gestion centralisée des settings de l'application.
 * Supporte le rechargement automatique lorsque le fichier golflc_settings.properties change.
 */

public final class Settings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, String> SETTINGS = new HashMap<>();

    private Settings() {
    }

    /** Chargement / rechargement explicite */
    public static synchronized void init() {
        try {
            LOG.debug("Initializing Settings");

            Properties properties = new Properties();

            try (InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("golflc_settings.properties")) {

                if (in == null) {
                    throw new IllegalStateException("golflc_settings.properties introuvable");
                }
                properties.load(in);
            }

            SETTINGS.clear();

            String userHome = System.getProperty("user.home");
            String userApp = properties.getProperty("settings.userapp", "");

            String userDir = userHome + userApp;
            String webapp = userDir + "/src/main/webapp/";
            String resources = webapp + "resources/";

            SETTINGS.put("EXECUTION", properties.getProperty("settings.execution", ""));
            SETTINGS.put("USER_HOME", userHome);
            SETTINGS.put("SMTP_PASSWORD", System.getenv("SMTP_PASSWORD"));
            SETTINGS.put("SMTP_SERVER", System.getenv("SMTP_SERVER"));
            SETTINGS.put("SMTP_USERNAME", System.getenv("SMTP_USERNAME"));

            SETTINGS.put("BATCH", userDir + "/InputBatchFiles/");
            SETTINGS.put("WEBAPP", webapp);
            SETTINGS.put("RESOURCES", resources);
            SETTINGS.put("IMAGES_LIBRARY", resources + "images/");
            SETTINGS.put("PHOTOS_LIBRARY", resources + "images/photos/");
            SETTINGS.put("THUMBNAILS_LIBRARY", resources + "images/thumbnails/");
            SETTINGS.put("WHS_CALCULATIONS", resources + "calculations/");
            SETTINGS.put("TARGET", userDir + "/target");

       // non secure    SETTINGS.forEach((k, v) -> LOG.debug(k + " = " + v));
            
            SETTINGS.forEach((k, v) -> {
                if (isSensitiveKey(k)) {
                    LOG.debug(k + " = ********");
            } else {
                LOG.debug(k + " = " + v);
            }
            });

            validateSmtpConfig();

        } catch (Exception e) {
            String msg = "Fatal error in Settings.init(): " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
        }
         
    } //end init
    
private static void validateSmtpConfig() {
    if (SETTINGS.get("SMTP_SERVER") == null ||
        SETTINGS.get("SMTP_USERNAME") == null ||
        SETTINGS.get("SMTP_PASSWORD") == null) {

        throw new IllegalStateException(
            "SMTP configuration incomplete: check environment variables");
    }
}
    public static String get(String key) {
        return SETTINGS.get(key);
    }

    /** Snapshot immuable pour l’UI */
    public static Map<String, String> snapshot() {
        return Map.copyOf(SETTINGS);
    }
    
    private static boolean isSensitiveKey(String key) {
    return key.contains("PASSWORD")
        || key.contains("SECRET")
        || key.contains("TOKEN");
}
   
} // end class

