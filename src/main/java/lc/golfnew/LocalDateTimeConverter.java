
package lc.golfnew;

//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * Faces converter for support of LocalDate
 * http://jj-blogger.blogspot.be/2015/06/utilizing-java-8-date-time-api-with-jsf.html
 * @author Juneau
 */
@FacesConverter(value="localDateTimeConverter")
public class LocalDateTimeConverter implements javax.faces.convert.Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
          return LocalDateTime.parse(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        LocalDateTime dateValue = (LocalDateTime) value;
     //   return dateValue.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
     return dateValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

} // end class