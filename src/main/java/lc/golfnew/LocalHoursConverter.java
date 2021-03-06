
package lc.golfnew;

import static interfaces.GolfInterface.ZDF_HOURS;
import java.time.LocalDate;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * Faces converter for support of LocalDate
 * http://jj-blogger.blogspot.be/2015/06/utilizing-java-8-date-time-api-with-jsf.html
 * @author Juneau
 */
@FacesConverter(value="localHoursConverter")
public class LocalHoursConverter implements javax.faces.convert.Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
          return LocalDate.parse(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        LocalDate dateValue = (LocalDate) value;
        
     //   return dateValue.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
     return dateValue.format(ZDF_HOURS);
    }
    
}