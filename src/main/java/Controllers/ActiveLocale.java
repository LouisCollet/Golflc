
package Controllers;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;

@Named("activeLocale")
@SessionScoped // fix multi-user 2026-03-07 — was @ApplicationScoped (shared locale bug)

public class ActiveLocale implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject private LanguageController languageController; // fix multi-user 2026-03-07

    private Locale currentLocale;
    private List<Locale> supportedLocales;

    public ActiveLocale() { } // constructeur public obligatoire

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        supportedLocales = Faces.getSupportedLocales();
        LOG.debug("supportedLocales = {}", supportedLocales);
        // Use default locale (EN) — not supportedLocales.get(0) which was FR
        currentLocale = Faces.getDefaultLocale();
        LOG.debug("initial locale = {}", currentLocale);
    } // end method

    public void reload() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        PrimeFaces.current().executeScript("window.location.reload(true);");
        LOG.debug("page reloaded");
    } // end method

    public Locale getCurrentLocale() {
        return currentLocale;
    } // end method

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    } // end method

    public String getDisplayLanguage(Locale locale) {
        return locale.getDisplayLanguage(locale).replace("fran", "Fran").replace("espa", "Espa");
    } // end method

    public String getLanguageTag() {
        return StringUtils.capitalize(currentLocale.getDisplayLanguage(currentLocale));
    } // end method

    public void setLanguageTag(String language) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with = {}", language);
        if (language == null || language.isBlank()) {
            LOG.debug("empty language, ignoring");
            return;
        }
        currentLocale = Locale.forLanguageTag(language);
        LOG.debug("current Locale = {}", currentLocale);
        languageController.setLanguage(language); // fix multi-user 2026-03-07 — was static call
    } // end method

    public List<Locale> getAvailableLocales() {
        return supportedLocales;
    } // end method

    private static final Map<String, String> LANG_TO_FLAG = Map.of(
            "en", "gb",
            "fr", "fr",
            "nl", "nl",
            "de", "de",
            "es", "es",
            "us", "us"
    );

    public String getFlagClass(Locale locale) {
        String code = LANG_TO_FLAG.getOrDefault(locale.getLanguage(), locale.getLanguage());
        return "flag flag-" + code + " ff-sm";
    } // end method
} // end class
