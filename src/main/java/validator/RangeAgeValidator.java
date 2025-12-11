
package validator;
import static interfaces.Log.LOG;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator(value="rangeAgeValidator")
public class RangeAgeValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        LOG.debug("entering validate.RangeAgeValidator ");
        LOG.debug("component = " + component.getFamily());
        LOG.debug("value = " + value.toString());
        
        Object otherValue = component.getAttributes().get("equalsValue");

  //      String field1Id = (String) component.getAttributes().get("cotisation:MembersItem1");
   //      LOG.debug("field1Id = " + field1Id);
  //      UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
  //      LOG.debug("passComponent = " + passComponent);

    
 //       if (value == null || otherValue == null) {
 //           return; // Let required="true" handle.
 //       }

        if (!value.equals(otherValue)) {
            FacesMessage msg =
                    new FacesMessage("validation error in RangeAgeValidator");
            LOG.debug("validation error in RangeAgeValidator");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

} //end class
    
