package Controllers;

import context.ApplicationContext;
import entite.Club;
import entite.Country;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.primefaces.event.SelectEvent;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;


@Named("countryC")
@ApplicationScoped 
// @SessionScoped
public class CountryController implements Serializable{
    @Inject private service.CountryService countryService;
   private Map<String, String> countriesMap = null; // migrated from static 2026-03-22
   private List<Country> countries;
   @Inject private ApplicationContext appContext;
      private Country country;

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

      
    public List<Country> getCountries() {
        LOG.debug("entering getCountries for countries = {}", countries);
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }
        
    public CountryController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        countriesMap = createMap(Locale.ENGLISH);
        countries = countryService.getCountries(); // migrated 2026-02-28 — was new service.CountryService()
    } // end method
     
    public Map<String, String> getCountriesMap() {
  //      LOG.debug("entering getCountriesMap()");
  //      LOG.debug("languagesMap getted = {}", countriesMap.toString());
    return countriesMap;
}

    public void setCountriesMap(Map<String, String> countriesMap) { // migrated from static 2026-03-22
        this.countriesMap = countriesMap;
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


public String getExtendedCountry(String S2) // migrated from static 2026-03-22
{  // from "HU" finding "Hungary"
  try{
//      LOG.debug("getExtendedCountry - searching for = {}", S2);
  // tester si null 
      String ret = null;
         for (Map.Entry<String,String> entry : countriesMap.entrySet()){
   //         LOG.debug("entry.getKey = {}", entry.getKey());
   //         LOG.debug("entry.getValue = {}", entry.getValue());
            if (entry.getValue().equals(S2)){
                ret = entry.getKey();
      //          LOG.debug("value founded = {}", entry.getValue());
      //          LOG.debug("key founded = {}", entry.getKey());
            } 
        } // end for
  //       LOG.debug("return extendedCountry = {}",  ret);
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
      LOG.debug("entering createMap with Locale = {}", locale);
        Map<String, String> map = new LinkedHashMap<>(); // preserve insertion order
	String[] locales = Locale.getISOCountries();
        for(String countryCode : locales){
		//Locale obj = new Locale("", countryCode); // deprecation java 19
                Locale obj = Locale.of("", countryCode);
             //   LOG.debug("item 1 = {}", obj.getCountry());
             //   LOG.debug("item 2 = {}", obj.getDisplayCountry());
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
/*
public List<Country> completeCountry(String query) { // autocomplete used in club.xhtml/player.xhtml
    LOG.debug("entering CountryController completeCountry with query = {}", query);
        String queryLowerCase = query.toLowerCase();
  //      LOG.debug("countryService = {}", countryService);
           if(countryService == null){
               LOG.debug("countryService was null");
                countryService = new CountryService();
  //             LOG.debug("new countryService = {}", countryService);
          }
        List<Country> countries = countryService.getCountries();
        LOG.debug("returned from completeCountry = {}", countries);
        return countries.stream()
                .filter(t -> t.getName()
                        .toLowerCase()
                        .contains(queryLowerCase))
                        .collect(Collectors.toList());
 //       return countries;
    } // end method
*/
public List<Country> completeCountry(String query) {
        try {
             LOG.debug("entering CountryController completeCountry with query = {}", query);
            List<Country> allCountries = countryService.getCountries();
            if (allCountries == null || allCountries.isEmpty()) {
                LOG.warn("Country list is empty from CountryService");
                return Collections.emptyList();
            }

            // Query vide (dropdown ouvert sans frappe) → retourner toute la liste
            if (query == null || query.trim().isEmpty()) {
                return allCountries;
            }

            String lowerQuery = query.toLowerCase();
            List<Country> filtered = allCountries.stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(lowerQuery))
                          || (c.getCode() != null && c.getCode().toLowerCase().contains(lowerQuery)))
                .limit(20)
                .collect(Collectors.toList());

            LOG.debug("Found {} countries matching '{}'", filtered.size(), query);
            return filtered;
        } catch (Exception e) {
            LOG.error("Error in completeCountry", e);
            return Collections.emptyList();
        }
    } // end method


//public void onCountrySelect(SelectEvent<String> event) {
//        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Country Selected", event.getObject()));
//    }

    public void onCountrySelect(SelectEvent<String> event) {
        try {
            LOG.debug("entering onCountrySelect for Club");
            String countrySelected = event.getObject();
            String msg = "country selected = " + countrySelected;
            LOG.debug(msg);

            if (countrySelected == null || countrySelected.trim().isEmpty()) {
                LOG.warn("Country code is null or empty");
                showMessageInfo("Invalid country selection");
                return;
            }

            List<Country> allCountries = countryService.getCountries();
            Country selectedCountry = allCountries.stream()
                .filter(c -> c.getCode() != null &&
                            c.getCode().equalsIgnoreCase(countrySelected))
                .findFirst()
                .orElse(null);

            if (selectedCountry == null) {
                LOG.warn("Country not found for code: {}", countrySelected);
                showMessageInfo("Country not found: " + countrySelected);
                return;
            }

            this.country = selectedCountry;

            Club club = appContext.getClub();               // ✅ current supprimé

            if (club != null && club.getAddress() != null) {
                // Code original : club.getAddress().getCountry().setCode(countrySelected)
                club.getAddress().getCountry().setCode(countrySelected);
                LOG.debug("club address is now = {}", club.getAddress());
                String confirmMsg = String.format("Country selected: %s (%s)",
                                                 selectedCountry.getName(),
                                                 countrySelected);
                showMessageInfo(confirmMsg);
            } else {
                LOG.warn("Club or club address is null");
                showMessageInfo("Cannot set country: club address not initialized");
            }
        } catch (Exception e) {
            String errorMsg = "Error in onCountrySelect: " + e.getMessage();
            LOG.error(errorMsg, e);
            showMessageFatal(errorMsg);
        }
    }

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class