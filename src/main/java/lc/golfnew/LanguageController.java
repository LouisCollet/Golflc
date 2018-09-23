/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package lc.golfnew;

import java.io.Serializable;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("languageC")
@SessionScoped()

public class LanguageController implements Serializable, interfaces.Log
{
private static final long serialVersionUID = 1L;

private static Locale locale = null;

@PostConstruct
public void init()
{
    locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
     //   LOG.info("from Postconstruct locale = " + locale);
}

public Locale getLocale()
    {  //  LOG.info("getLocale = " + locale);
        return locale;
    }

public String getLanguage()
    {  // LOG.info("getLanguage, getCountry = " + locale.getCountry() );
       // LOG.info("getLanguage, getDisplayCountry = " + locale.getDisplayCountry() );
       // LOG.info("getLanguage, getDisplayLanguage = " + locale.getDisplayLanguage() );
       // LOG.info("getLanguage, getDisplayName = " + locale.getDisplayName() );
        return locale.getLanguage();
    }

    /**
	 * Sets the current {@code Locale} for each user session
	 * 
	 * @param languageCode - ISO-639 language code
	 */
public static void setLanguage(String language)
{
    LOG.info("entering setLanguage with existing locale = " + locale + "  has to change to : " + language);
   locale = new Locale(language);
   FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      LOG.info("language switched to = " + locale);
 // new 18/08/2013 - enlevé 25/08/2013
   // refreshPage();
      
      
      
}

protected static void refreshPage()
{
 FacesContext fc = FacesContext.getCurrentInstance();
 String refreshpage = fc.getViewRoot().getViewId();
 ViewHandler ViewH =fc.getApplication().getViewHandler();
 UIViewRoot UIV = ViewH.createView(fc,refreshpage);
 UIV.setViewId(refreshpage);
 fc.setViewRoot(UIV);
    LOG.info("page refreshed !");
 }



} // end class
