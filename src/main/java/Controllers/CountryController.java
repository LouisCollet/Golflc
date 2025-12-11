package Controllers;

import entite.Country;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import services.CountryService;

// not used anymore 29/12/2022

@Named("countryC")
@ApplicationScoped 
// @SessionScoped
public class CountryController implements Serializable{
    @Inject private CountryService countryService;
   private static Map<String, String> countriesMap = null;
   private List<Country> countries;

    public List<Country> getCountries() {
        LOG.debug("entering getCountries for countries = " + countries);
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }
        
public CountryController(){  // constructor
    LOG.debug("entering CountryController");
  //       LOG.debug("step 1");
        countriesMap = createMap(Locale.ENGLISH); // à modifier en fonction user ? non default = "en"
           LOG.debug("just before getCountries");
   //     countries = new services.CountryService().init();
        countries = new services.CountryService().getCountries();
}
     
    public Map<String, String> getCountriesMap() {
  //      LOG.debug("entering getCountriesMap()");
  //      LOG.debug("languagesMap getted = " + countriesMap.toString());
    return countriesMap;
}

    public static void setCountriesMap(Map<String, String> countriesMap) {
        CountryController.countriesMap = countriesMap;
    }
    
//public boolean getListOfCountries(Locale locale) {
    
    // non ! alller les chercher dans service
 public List<String> getListOfCountries() {
    LOG.debug("entering ListOfCountries");
	String[] locales = Locale.getISOCountries();
        List<String> list = new ArrayList<>();
	for (String countryCode : locales) {
//	    Locale loc = new Locale("", countryCode); // deprecation java 19
            Locale loc = Locale.of("", countryCode);
            list.add(loc.getCountry() + "-" + loc.getDisplayCountry());
//	    LOG.debug("Country Code = " + loc.getCountry()
//		+ ", Locale Country Name = " + loc.getDisplayCountry());
	 }
        return list;
    }


public static String getExtendedCountry(String S2)
{  // from "HU" finding "Hungary"
  try{
//      LOG.debug("getExtendedCountry - searching for = " + S2);
  // tester si null 
      String ret = null;
         for (Map.Entry<String,String> entry : countriesMap.entrySet()){
   //         LOG.debug("entry.getKey = " + entry.getKey());
   //         LOG.debug("entry.getValue = " + entry.getValue());
            if (entry.getValue().equals(S2)){
                ret = entry.getKey();
      //          LOG.debug("value founded = " + entry.getValue());
      //          LOG.debug("key founded = " + entry.getKey());
            } 
        } // end for
  //       LOG.debug("return extendedCountry = " +  ret);
    return ret;
  } catch (Exception e) {
            String msg = "Â£ Exception in getExendedCountry = " + e.getMessage();
            LOG.error(msg);
        //    LCUtil.showMessageFatal(msg);
            return null;
        }  
};


private Map<String, String> createMap(Locale locale) {
try{
      LOG.debug("entering createMap with Locale = " + locale);
        Map<String, String> map = new LinkedHashMap<>(); // preserve insertion order
	String[] locales = Locale.getISOCountries();
        for(String countryCode : locales){
		//Locale obj = new Locale("", countryCode); // deprecation java 19
                Locale obj = Locale.of("", countryCode);
             //   LOG.debug("item 1 = " + obj.getCountry());
             //   LOG.debug("item 2 = " + obj.getDisplayCountry());
                 map.put(obj.getCountry()+" - " + obj.getDisplayCountry(locale), obj.getCountry()); 
                 // affiché : format BE - Belgium
        } // end for 
          return map;
    } catch (Exception e) {
            String msg = "Â£ Exception in createMap = " + e.getMessage();
            LOG.error(msg);
        //    LCUtil.showMessageFatal(msg);
            return null;
        } finally {
  //          LOG.debug("Done");
        }      
    } // end method

public List<Country> completeCountry(String query) { // autocomplete used in club.xhtml    player.xhtml
    LOG.debug("entering CountryController completeCountry with query = " + query);
        String queryLowerCase = query.toLowerCase();
  //      LOG.debug("countryService = " + countryService);
           if(countryService == null){
               LOG.debug("countryService was null");
                countryService = new CountryService();
  //             LOG.debug("new countryService = " + countryService);
          }
        List<Country> countries = countryService.getCountries();
        LOG.debug("returned from completeCountry = " + countries);
        return countries.stream()
                .filter(t -> t.getName()
                        .toLowerCase()
                        .contains(queryLowerCase))
                        .collect(Collectors.toList());
 //       return countries;
    }

public void onCountrySelect(SelectEvent<String> event) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country Selected", event.getObject()));
    }

  void main() {
	CountryController cc = new CountryController();
	var v = cc.createMap(Locale.ENGLISH);
        LOG.debug("createMap = " + v.toString());
	//var list = obj.getListOfCountries(); //Locale.ENGLISH
        var list = cc.completeCountry(""); //Locale.ENGLISH
        
   //     LOG.debug(" " + b);
        list.forEach(item -> LOG.debug("list of countries = " + item));  // java 8 lambda
     } //end main 

}