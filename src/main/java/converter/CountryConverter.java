
package converter;

import entite.Country;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import service.CountryService;

/**
 * Converter pour Country
 * Convertit entre le code ISO (String) et l'objet Country
 */
@FacesConverter(value = "countryConverter", managed = true)
public class CountryConverter implements Converter<Country> {

    @Inject
    private CountryService countryService;

    @Override
    public Country getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Chercher le pays par code ISO dans la liste
        return countryService.getCountries().stream()
            .filter(c -> c.getCode() != null && c.getCode().equals(value))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Country country) {
        if (country == null) {
            return "";
        }
        return country.getCode();  // Retourne le code ISO (ex: "BE", "FR")
    }
}