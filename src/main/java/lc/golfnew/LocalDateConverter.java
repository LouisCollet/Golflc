
package lc.golfnew;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * Faces converter for support of LocalDate
 * http://jj-blogger.blogspot.be/2015/06/utilizing-java-8-date-time-api-with-jsf.html
 * @author Juneau
 */
@FacesConverter(value="localDateConverter")
public class LocalDateConverter implements javax.faces.convert.Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
          return LocalDate.parse(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        LocalDate dateValue = (LocalDate) value;
        
     //   return dateValue.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
     return dateValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
}