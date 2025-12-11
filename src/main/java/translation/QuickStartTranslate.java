/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package translation;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;
import static interfaces.GolfInterface.GoogleApiKey;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class QuickStartTranslate{
    public static void main(String[] arguments) throws IOException, GeneralSecurityException{
        Translate translate = new Translate.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), null)
                // Set your application name
                .setApplicationName("Stackoverflow-Example")
                
                .build();
        
        Translate.Translations.List list = translate.new Translations().list(
                Arrays.asList(
                        // Pass in list of strings to be translated
                        "Hello World",
                        "How to use Google Translate from Java")
                ,"ES");// Target language

        // TODO: Set your API-Key from https://console.developers.google.com/
        list.setKey(GoogleApiKey);
        TranslationsListResponse response = list.execute();
        for (TranslationsResource translationsResource : response.getTranslations()){
            LOG.debug(translationsResource.getTranslatedText());
        }
    }
}