package Controllers;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("languageC")
@SessionScoped // fix multi-user 2026-03-07 — was @ApplicationScoped (shared locale bug)

public class LanguageController implements Serializable {
    private static final long serialVersionUID = 1L;
    private Locale locale = null;

    public LanguageController() { } // constructeur public obligatoire

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            // Accept-Language header from browser
            Locale browserLocale = context.getExternalContext().getRequestLocale();
            LOG.debug(methodName + " - browser locale = " + browserLocale);
            // check if supported in faces-config.xml
            if (context.getApplication().getSupportedLocales() != null) {
                String browserLang = browserLocale.getLanguage();
                Locale defaultLocale = context.getApplication().getDefaultLocale();
                locale = defaultLocale; // fallback
                if (defaultLocale != null && defaultLocale.getLanguage().equals(browserLang)) {
                    locale = defaultLocale;
                } else {
                    var supported = context.getApplication().getSupportedLocales();
                    while (supported.hasNext()) {
                        Locale sup = supported.next();
                        if (sup.getLanguage().equals(browserLang)) {
                            locale = sup;
                            break;
                        }
                    }
                }
                LOG.debug(methodName + " - resolved locale = " + locale);
            } else {
                locale = browserLocale;
                LOG.debug(methodName + " - no supported locales configured, using browser locale = " + locale);
            }
        } else {
            locale = Locale.ENGLISH;
            LOG.debug(methodName + " - no FacesContext, defaulting to EN");
        }
    } // end method

    public Locale getLocale() {
        return locale;
    } // end method

    public void setLocale(Locale locale) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.locale = locale;
        LOG.debug(methodName + " - locale set = " + this.locale);
    } // end method

    public String getLanguage() {
        return locale.getLanguage();
    } // end method

    public void setLanguage(String language) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with existing locale = " + locale);
        LOG.debug(methodName + " - will be changed to : " + language);
        setLocale(Locale.of(language));
        LOG.debug(methodName + " - DisplayLanguage is now = " + locale.getDisplayLanguage());
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getViewRoot() != null) {
            fc.getViewRoot().setLocale(locale);
            LOG.debug(methodName + " - FacesContext locale is now = " + fc.getViewRoot().getLocale());
        }
    } // end method
} // end class
