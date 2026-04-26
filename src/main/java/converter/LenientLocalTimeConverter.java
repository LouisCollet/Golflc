
package converter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@FacesConverter(value = "lenientLocalTimeConverter", managed = true)
public class LenientLocalTimeConverter implements Converter<LocalTime> {

    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim();

        // Normalisation : "7:30" → "07:30"
        if (value.matches("^\\d{1}:\\d{2}$")) {
            value = "0" + value;
        }

        return LocalTime.parse(value, OUTPUT);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalTime value) {
        if (value == null) {
            return "";
        }
        return value.format(OUTPUT);
    }
}