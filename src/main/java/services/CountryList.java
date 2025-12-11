/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import static interfaces.Log.LOG;
import java.util.Locale;
public class CountryList {

void main() {
    String[] countryCodes = Locale.getISOCountries();
      LOG.debug("Number of countries and regions: " + countryCodes.length);

for (String countryCode : countryCodes) {
    Locale locale = Locale.of("", countryCode); // language, country
    String code = locale.getCountry();
    String name = locale.getDisplayCountry();
    String language= locale.getDisplayLanguage();
    LOG.debug("code = " + code + " name = " + name + " language = " + language + countryCode);
}
}
}