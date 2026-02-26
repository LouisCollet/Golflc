
package utils;

import static interfaces.Log.LOG;
import java.util.Locale;

public class CountryUtils {

    /**
     * Retourne le code ISO 3166-1 alpha-2 en minuscules pour un nom de pays donné.
     * Exemple : "BELGIUM" -> "be"
     * @param countryName Nom complet du pays
     * @return code ISO minuscule, ou null si non trouvé
     */
public static String getCountryCode(String countryName) {
    if (countryName == null || countryName.isEmpty()) {
        return null;
    }

    for (String iso : Locale.getISOCountries()) {
        Locale locale = new Locale.Builder()
                .setRegion(iso)
                .build();

        if (locale.getDisplayCountry(Locale.ENGLISH)
                .equalsIgnoreCase(countryName)) {
            return iso.toLowerCase();
        }
    }
    LOG.debug("countryName not found : " + countryName);
    return null;
}


    // Test rapide
    public static void main(String[] args) {
        String country = "BELGIUM";
        String code = getCountryCode(country);
        System.out.println(country + " -> " + code); // Belgium -> be
    }
}
