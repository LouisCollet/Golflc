package Controllers;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("languageC")
@ApplicationScoped // mod 30-12-2022 @SessionScoped // nécessaire

public class LanguageController implements Serializable{   
private static final long serialVersionUID = 1L;
private static Locale locale = null;

@PostConstruct
public void init(){
    FacesContext context = FacesContext.getCurrentInstance();
    if (context != null && context.getViewRoot() != null) {
        locale = context.getViewRoot().getLocale();
        LOG.debug("from Postconstruct init() locale = " + locale);
    } else {
        locale = Locale.FRENCH;
        LOG.debug("from Postconstruct init() - no FacesContext, defaulting to FR");
    }
 //     LOG.debug("from Postconstruct current = "
 //                + context.getApplication().getViewHandler().calculateLocale(context).toString());
}
// enlevé static
 public Locale getLocale() {
        return locale;
    }

 public static void setLocale(Locale locale) {
         LanguageController.locale = locale;
        LOG.debug("locale set = " + LanguageController.locale);
    }

public static String getLanguage() {
        return locale.getLanguage();
    }

public static void setLanguage(String language){
      LOG.debug("entering setLanguage with existing locale = " + locale);
      LOG.debug("  will be changed to : " + language);
   setLocale(Locale.of(language));
      LOG.debug("DisplayLanguage is now = " + locale.getDisplayLanguage());
   FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      LOG.debug("FacesContext locale is now = " + FacesContext.getCurrentInstance().getViewRoot().getLocale());
}
} // end class