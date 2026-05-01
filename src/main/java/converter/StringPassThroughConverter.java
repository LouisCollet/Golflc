package converter;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@ApplicationScoped
@FacesConverter(value = "stringPassThroughConverter", managed = true) // used in ground_condition_wizard.xhtml
public class StringPassThroughConverter implements Converter<String> {

    @Override
    public String getAsObject(FacesContext ctx, UIComponent comp, String value) {
        return (value == null || value.isEmpty()) ? null : value;
    } // end method

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, String value) {
        return value == null ? "" : value;
    } // end method

} // end class
