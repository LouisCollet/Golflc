
package Controllers;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
//import org.apache.maven.shared.utils.StringUtils;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;

@Named("activeLocale")
@ApplicationScoped //@SessionScoped // mod 09/05/2022

public class ActiveLocale implements Serializable {
    private Locale currentLocale;
 //   private List<Locale> availableLocales;
    private List<Locale> supportedLocales;

@PostConstruct
public void init() {
  //     LOG.debug("entering ActiveLocale.init");
    supportedLocales = Faces.getSupportedLocales(); // from faces-config  <locale-config>
      LOG.debug("supportedLocales = " + supportedLocales);
    currentLocale =  supportedLocales.get(0); // first one
 //     LOG.debug("First currentLocale from supportedLocales = " + currentLocale);
 //   supportedLocales.forEach(item -> LOG.debug("list of languages in the currentLocale= " + item.getDisplayLanguage(currentLocale)));
 //   supportedLocales.forEach(item -> {
  //      LOG.debug("list of languages in the currentLocale= " + item);
  

   // });
}

public void reload() {     // starts javascript
  //   mod 23-05-2024 PrimeFaces.current().executeScript("location.replace(location);");
     PrimeFaces.current().executeScript("window.location.reload(true);");  // Force a reload, bypassing the browser cache
        LOG.debug("page reloaded ! " ); //+ PrimeFaces.current().executeScript('location.replace(location);'));
}




    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

public String getDisplayLanguage(Locale locale) {
 // LOG.debug("entering getDisplayLanguage swith String = " + locale);
 // correction bug java : français remplacé par Français, espanol par Espanol
   return locale.getDisplayLanguage(locale).replace("fran", "Fran").replace("espa", "Espa");

}

public String getLanguageTag() {
 ///   LOG.debug("getLanguageTag 1 = " + StringUtils.capitalise(currentLocale.getDisplayLanguage(currentLocale))); 
///    LOG.debug("getLanguageTag 2 = " + currentLocale.getDisplayLanguage(currentLocale).toUpperCase()); 
//   return current.toLanguageTag();
// bug java : français remplacé par Français, espanol par Espanol ...
   return StringUtils.capitalize(currentLocale.getDisplayLanguage(currentLocale));
}
public void setLanguageTag(String language) {
         LOG.debug("entering setLanguaeTag with = " + language);
   currentLocale = Locale.forLanguageTag(language);
         LOG.debug("current Locale = " + currentLocale);
 // on modifie le Controller de base !!
      LanguageController.setLanguage(language);
}
public List<Locale> getAvailableLocales() {
 //  return availableLocales;
   return supportedLocales; // mod 30-12-2022

}
} // end Class