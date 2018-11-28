
package lc.golfnew;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class Constants
{
    private Constants()
    {
        // 
    }
    public static final int BUFFER_SIZE = 1024;
    // user.home = c:/users/Collet
//    String path = application.getRealPath("/");

    public static final String USER_DIR = System.getProperty("user.home") + "/Documents/NetBeansProjects/GolfWfly/";
 //   public static final String USER_DIR = "C:/Users/Collet/Documents/NetBeansProjects/GolfWfly/";
//    public static final String images_library = System.getProperty("user.dir") + "/src/main/webapp/resources/images/";
    public static final String images_library = USER_DIR + "/src/main/webapp/resources/images/";
    
    public static final String AP_TARGET = USER_DIR + "/target"; //new 08/03/2017
    // in widlfly : user.dir  = C:\ProgramData\wildfly-10.1.0.Final\bin 
    // in glassfish : netbeansprojects\Golfwildfly ...C:\Users\Collet\Documents\NetBeansProjects\GolfWfly\src\main
 //   public static final String photos_library = System.getProperty("user.dir") + "/src/main/webapp/resources/images/photos/";
 //   public static final String photos_library = USER_DIR + "/src/main/webapp/resources/images/photos/";
    public static final String photos_library = images_library + "/photos/";
    public static final Path path_images_library = Paths.get(photos_library);
 //   public static final String thumbnails_library = System.getProperty("user.dir") + "/src/main/webapp/resources/images/thumbnails/";
 //   public static final String thumbnails_library = USER_DIR + "/src/main/webapp/resources/images/thumbnails/";
    public static final String thumbnails_library = images_library + "/thumbnails/";
    static final double YARD_TO_METER = .9144;
    
  //  static final String GOOGLE_API_KEY = "AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc";
    static final String GOOGLE_API_KEY = "AIzaSyB8N9pZyPmYCSr0d3sTf9n8HHlvHlTRYLI";
 //   static final DateTimeFormatter dtf_HHmm = DateTimeFormatter.ofPattern("HH:mm");
    static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
}
