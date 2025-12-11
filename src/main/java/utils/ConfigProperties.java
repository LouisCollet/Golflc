package utils;

import java.io.*;

public class ConfigProperties implements interfaces.GolfInterface, interfaces.Log
{
//private static final Logger log = Logger.getLogger("golflc");
      //private static Properties prop = new Properties();

public ConfigProperties() // constructor
{
}
public static void storeProperties() throws FileNotFoundException, IOException{
try{
       FileOutputStream fos = new FileOutputStream("propertiesDir" + "/golflc_config.properties");
    //set the properties value
    //http://docs.oracle.com/javase/1.4.2/docs/api/java/util/Properties.html
    java.util.Properties prop = new java.util.Properties();
    //InputStream is = null;

        LOG.debug("store properties 01");
    prop.setProperty("324713", "Administrator");
    // voir dans GolfInterface de utilspackage
 //       LOG.debug(" -- golfDir = " + golfDir);

        // default = sous user.dir = c:\glassfish3\glassfish\domains\domain1
    prop.store(fos, "--- GolfLC Application config Properties");
    LOG.debug("store properties 02");
}
catch( FileNotFoundException fnfe)    {
      LOG.error("FileNotFoundException " + fnfe);
      throw fnfe;    }
catch( IOException ioe)    {
      LOG.error("IOFoundException " + ioe);
      throw ioe;
    }
finally
{
     LOG.debug("fos : closed thru finally !");
    }
}
//////////////////
public static boolean loadProperties(String in_playerId) throws FileNotFoundException, IOException{
try(
    FileInputStream fis = new FileInputStream("propertiesDir" + "golflc_config.properties")) {
    java.util.Properties prop = new java.util.Properties();
        LOG.debug("load properties, in_playerId = "  + in_playerId);
        LOG.debug(" -- propertiesDir = " + "propertiesDir");
    prop.load(fis);
    String key = "testORproduction";
        LOG.debug(" execution Test OR Production ? = " + prop.getProperty(key,"testORproduction property not found") );

//get the properties value
    //The method returns the default value argument ("User")if the property is not found.

    if (prop.getProperty(in_playerId,"User").equals("Administrator")){
        LOG.debug("isAdministrator = " + in_playerId);
            return true;
    }else{
            LOG.debug("is NOT Administrator = " + in_playerId);
            return false;
    }

}
catch( FileNotFoundException fnfe){
      LOG.error("FileNotFoundException " + fnfe);
      throw fnfe;
 }
catch( IOException ioe){
      LOG.error("IOFoundException " + ioe);
      throw ioe;
    }
finally{
    LOG.debug("fis closed thru finally !");
}
}

void main(){
;
  } // end main
}  //end class