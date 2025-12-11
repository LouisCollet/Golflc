
package translation;


// Imports the Google Cloud Translation library.
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import static interfaces.Log.LOG;
import java.io.IOException;

// ne fopnctionne pas pour credentials
public class TranslateText {

  // Set and pass variables to overloaded translateText() method for translation.
 /* public static void translateText() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR-PROJECT-ID";
    // Supported Languages: https://cloud.google.com/translate/docs/languages
    String targetLanguage = "your-target-language";
    String text = "your-text";
    translateText(projectId, targetLanguage, text);
  }
*/
  // Translate text to target language.
  public static void translateText(String projectId, String targetLanguage, String text)
      throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      // Supported Locations: `global`, [glossary location], or [model location]
      // Glossaries must be hosted in `us-central1`
      // Custom Models must use the same location as your model. (us-central1)
      LocationName parent = LocationName.of(projectId, "global");

      // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
      TranslateTextRequest request = TranslateTextRequest.newBuilder()
              .setParent(parent.toString())
              .setMimeType("text/plain")
              .setTargetLanguageCode(targetLanguage)
              .addContents(text)
              .build();

      TranslateTextResponse response = client.translateText(request);

      // Display the translation for each input text provided
      for (Translation translation : response.getTranslationsList()) {
        LOG.debug("Translated text: %s\n", translation.getTranslatedText());
      }
    }
  }
  
 void main(){
  try{
  //  ex1.capture("https://www.selenium.dev/selenium/web/web-form.html");
 //   boolean b = new ExampleOne().capture("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/tee.xhtml");
     translateText("projectId", "fr", "texte à traduire");
//   LOG.debug("result in main = " + b) ;
    
 //   LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in TranslateText.main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
} // end main//
  
  
}