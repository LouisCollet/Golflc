
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

// http://www.luv2code.com/2015/10/13/jsf-setting-itemlabel-with-selectitems/
// https://www.mkyong.com/java/display-a-list-of-countries-in-java/

@Named("listCtry")
@SessionScoped
public class ListCountry implements Serializable, interfaces.Log{
    
   private static Map<String, String> countriesMap = null; //new TreeMap<>();
     
public ListCountry(){  // constructor
    LOG.info("entering constructor ListCountry()");
  //       LOG.info("step 1");
        countriesMap = createMap(Locale.ENGLISH); // à modifier en fonction user ? non default = "en"
}
     
    public Map<String, String> getCountriesMap() {
  //      LOG.info("entering getCountriesMap()");
  //      LOG.info("languagesMap getted = " + countriesMap.toString());
    return countriesMap;
}

    public void setCountriesMap(Map<String, String> countriesMap) {
        ListCountry.countriesMap = countriesMap;
    }
 
    public static void main(String[] args) {
	ListCountry obj = new ListCountry();
	obj.createMap(Locale.ENGLISH);
	obj.getListOfCountries(Locale.ENGLISH);
     } //end main 
    
public void getListOfCountries(Locale locale) {
	String[] locales = Locale.getISOCountries();
	for (String countryCode : locales) {
	    Locale obj = new Locale("", countryCode);
	    LOG.info("Country Code = " + obj.getCountry()
		+ ", Locale Country Name = " + obj.getDisplayCountry(locale));
	 }
    }
public static String getExtendedCountry(String S2)
{  // from "HU" finding "Hungary"
  try{
      LOG.info("getExtendedCountry - searching for = " + S2);
  
      String ret = null;
         for (Map.Entry<String,String> entry : countriesMap.entrySet()){
   //         LOG.info("entry.getKey = " + entry.getKey());
   //         LOG.info("entry.getValue = " + entry.getValue());
            if (entry.getValue().equals(S2)){
                ret = entry.getKey();
                LOG.info("value founded = " + entry.getValue());
                LOG.info("key founded = " + entry.getKey());
            } //end if
        } // end for
         LOG.info("return extendedCountry = " +  ret);
    return ret;
  } catch (Exception e) {
            String msg = "Â£ Exception in gestExendedCountry = " + e.getMessage();
            LOG.error(msg);
        //    LCUtil.showMessageFatal(msg);
            return null;
        }  
};


private Map<String, String> createMap(Locale locale) {
try{
      LOG.info("entering createMap with Locale = " + locale);
        Map<String, String> c = new LinkedHashMap<>(); // preserve insertion order
	String[] locales = Locale.getISOCountries();
        for (String countryCode : locales)
        {
		Locale obj = new Locale("", countryCode);
             //   LOG.info("item 1 = " + obj.getCountry());
             //   LOG.info("item 2 = " + obj.getDisplayCountry());
                 c.put(obj.getCountry()+" - " + obj.getDisplayCountry(locale), obj.getCountry()); 
                 // affiché : format BE - Belgium
        } // end for 
          return c;
    } catch (Exception e) {
            String msg = "Â£ Exception in ListCountry = " + e.getMessage();
            LOG.error(msg);
        //    LCUtil.showMessageFatal(msg);
            return null;
        } finally {
  //          LOG.info("Done");
        }      
    } // end method
} // end class