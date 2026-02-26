
package service;

import entite.Country;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named // remis 24-0-6-2025 enlevé 16-02-2024 n'est utilisé que par CourseController
@ApplicationScoped // mod 24/12/2022
public class CountryService implements Serializable{
    private List<Country> countries;
   // private List<Country> countries2;
    private List<Country> locales;
public CountryService(){
   // LOG.debug("constructor CountryService");
}
    @PostConstruct
    public void init() {
   /*     LOG.debug("entering CountryService init()");
        countries = new ArrayList<>();
        String[] isoCodes = Locale.getISOCountries();
        for(int i = 0; i < isoCodes.length; i++) {
            countries.add(new Country(i, Locale.of("", isoCodes[i])));
        }
            LOG.debug("number of countries version  = " + countries.size());
            LOG.debug("list of countries version 2 = " + countries.toString());
        */
     // new 24-06-2025 https://showcase.primefaces.org/ui/input/autoComplete.xhtml?jfwid=bb4d1
        countries = CountryService.toCountryStream(Locale.getISOCountries())
                .sorted(Comparator.comparing(Country::getName))
                .collect(Collectors.toList());
        //    LOG.debug("list of countries version = " + countries.size() + " / " + countries.toString());
    }
// new 24-06-2025
    public static Stream<Country> toCountryStream(String... isoCodes) {
        return Stream.of(isoCodes)
                .map(isoCode -> Locale.of("", isoCode))
                .map(CountryService::toCountry);
    }
 // new 24-06-2025
    public static Country toCountry(Locale locale) {
        return CountryService.toCountry(locale, false);
    }
    public static Country toCountry(Locale locale, boolean rtl) {
        //use hash code from locale to have a reproducible ID (required for CountryConverter)
        return new Country(locale.hashCode(), locale, rtl);
    }
    
    public List<Country> getCountries() {
      //  LOG.debug("getCountries");
        return countries;
    }

    public List<Country> getLocales() {
        return new ArrayList<>(locales);
    }
 }

/*
package services;

import entite.Country;
import static interfaces.Log.LOG;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
//@SessionScoped
@ApplicationScoped // mod 24/12/2022
public class CountryService implements Serializable{

//    @Inject Country country;
    private List<Country> countries;
    private Map<Integer, Country> countriesAsMap;
    private List<Country> locales;
    private Map<Integer, Country> localesAsMap;
//    private int i = 0;
public CountryService(){
    LOG.debug("constructor CountryService");
 //   if(i == 0){
//        init();
 //       i++;
 //   }
    
}
    @PostConstruct
    public void init() {
        LOG.debug("entering CountryService init()");
        countries = new ArrayList<>();
   //     locales = new ArrayList<>();

        String[] isoCodes = Locale.getISOCountries();
            LOG.debug("number of isoCodes = " + isoCodes.length);
        
        for(int i = 0; i < isoCodes.length; i++) {
         //   Locale locale = new Locale("", isoCodes[i]);
            Locale locale = Locale.of("", isoCodes[i]);
            countries.add(new Country(i, locale));
        }
            LOG.debug("number of countries = " + countries.size());

    }

    public List<Country> getCountries() {
        return new ArrayList<>(countries);
    }

    public Map<Integer, Country> getCountriesAsMap() {
        if (countriesAsMap == null) {
            countriesAsMap = getCountries().stream().collect(Collectors.toMap(Country::getId, country -> country));
        }
        return countriesAsMap;
    }
    
    public List<Country> getLocales() {
        return new ArrayList<>(locales);
    }
    
    public Map<Integer, Country> getLocalesAsMap() {
        if (localesAsMap == null) {
            localesAsMap = getLocales().stream().collect(Collectors.toMap(Country::getId, country -> country));
        }
        return localesAsMap;
    }
}
*/