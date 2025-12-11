
package translation;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.Translate.Translations;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;
//import exceptions.HandledException;
import exceptions.LCException;
import static interfaces.GolfInterface.GoogleApiKey;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import utils.LCUtil;

public class FileTranslation{
      private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public static String translateFile(Path path, String targetLanguage) throws IOException, GeneralSecurityException {    
 try{
   //https://any-api.com/googleapis_com/translate/docs/translations/language_translations_list 
   // https://developers.google.com/resources/api-libraries/documentation/translate/v2/java/latest/com/google/api/services/translate/Translate.Builder.html
    List<String> data = null;
      try{   // test if file exists
///         data = Files.readAllLines(path);
    ///     return translateData(data, targetLanguage);
         return translateData(Files.readAllLines(path), targetLanguage);
     }catch (Exception ex){
         String msg = " <br/>££ Exception in reaAllLines " + ex;
         LOG.error(msg);
  //  throw new Exception(msg);
         LCUtil.showMessageFatal(msg);
  //  return msg;
}    
   Translate translate = new Translate.Builder(
                          GoogleNetHttpTransport.newTrustedTransport(),
                          GsonFactory.getDefaultInstance(),
                          null)
                // Set your application name
                .setApplicationName("GolfLc v1.0")
                .build();
   //     List<String> result = Files.readAllLines(Paths.get("c:/log/traduction_test.txt"));
        //Charset charset = Charset.forName("ISO-8859-1");
        //List<String> result = Files.readAllLines(Paths.get(filename), charset);
           LOG.debug("result = " + data);
        Translations.List list = translate.new Translations().list(data, "");
        list.setKey(GoogleApiKey);
        list.setSource("fr"); // si pas spécifié l'API la détermine
        list.setTarget(targetLanguage);
//        list.setPrettyPrint(Boolean.TRUE);
        list.setFormat("html");  // = default / alternative = "text"
        TranslationsListResponse response = list.execute();
  //         LOG.debug("source language detected = " + response.getTranslations().get(0).getDetectedSourceLanguage());
 //  List<String> translated = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(TranslationsResource resource : response.getTranslations()){
  //          LOG.debug("source language = " + resource.getDetectedSourceLanguage());
//            LOG.debug("translated text = " + resource.getTranslatedText());
            sb.append(resource.getTranslatedText());
 //           translated.add(resource.getTranslatedText());
        }
    return sb.toString();

 }catch (Exception ex){
    String msg = "Exception in " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
} // translate
    
 public static String translateList(List<String> list, String targetLanguage) throws IOException, GeneralSecurityException {
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
 try{
   //https://any-api.com/googleapis_com/translate/docs/translations/language_translations_list 
   // https://developers.google.com/resources/api-libraries/documentation/translate/v2/java/latest/com/google/api/services/translate/Translate.Builder.html

    if(targetLanguage == null){
        Exception e = new Exception(" = targetLanguage not completed"); 
        throw new LCException("LCException in : " + methodName, e);
    }
    return translateData(list, targetLanguage);
    
  }catch (Exception ex){
    String msg = "Exception in translateList" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
} // translate
 
 public static String translateData(List<String> data, String targetLanguage) throws IOException, GeneralSecurityException {
 try{
      Translate translate = new Translate.Builder(
                          GoogleNetHttpTransport.newTrustedTransport(),
                          GsonFactory.getDefaultInstance(),
                          null)
                // Set your application name
                .setApplicationName("GolfLc v1.0")
                .build();
   //     List<String> result = Files.readAllLines(Paths.get("c:/log/traduction_test.txt"));
        //Charset charset = Charset.forName("ISO-8859-1");
        //List<String> result = Files.readAllLines(Paths.get(filename), charset);
           LOG.debug("result = " + data);
        Translations.List list = translate.new Translations().list(data, "");
        list.setKey(GoogleApiKey);
        list.setSource("fr"); // si pas spécifié l'API la détermine
        list.setTarget(targetLanguage);
//        list.setPrettyPrint(Boolean.TRUE);
        list.setFormat("html");  // = default / alternative = "text"
        TranslationsListResponse response = list.execute();
  //         LOG.debug("source language detected = " + response.getTranslations().get(0).getDetectedSourceLanguage());
 //  List<String> translated = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(TranslationsResource resource : response.getTranslations()){
  //          LOG.debug("source language = " + resource.getDetectedSourceLanguage());
//            LOG.debug("translated text = " + resource.getTranslatedText());
            sb.append(resource.getTranslatedText());
 //           translated.add(resource.getTranslatedText());
        }
    return sb.toString();

 }catch (Exception ex){
    String msg = "Exception in " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
} // translate
 
   public static void main(String[] arguments) throws Exception { 
try{
   String f = "C:/Users/Collet/Documents/NetBeansProjects/GolfWfly/src/main/webapp/help/help_welcome.xhtml"; 
 //  File file = new File(f);
   Path path = Paths.get(f);
   String str = translateFile(path,"es");
           LOG.error("translated text = " + str);
 //  str = "123<b/>456LouisColletGolf<LC</h:outputText>";
       int firstIndex = str.indexOf("<br/>"); 
           LOG.debug("firstIndex = " + firstIndex);
       if(firstIndex == -1){
           LOG.debug("firstIndex not found");
       }
           
       int lastIndex = str.indexOf("</h:outputText>");
       if(lastIndex == -1){
           LOG.debug("lastIndex not found");
       }
           LOG.debug("lastIndex = " + lastIndex);
           
       LOG.debug("substring  = " + str.substring(firstIndex+5, lastIndex));

}catch (Exception ex){
    String msg = "Exception in main" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
 //   return null;
}
   }// end main
} // end class