
package validator;
import static interfaces.Log.LOG;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator(value="rangeAgeValidator")
public class RangeAgeValidator implements Validator {

    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        LOG.info("entering validate.RangeAgeValidator ");
        LOG.info("component = " + component.getFamily());
        LOG.info("value = " + value.toString());
        
        Object otherValue = component.getAttributes().get("equalsValue");

  //      String field1Id = (String) component.getAttributes().get("cotisation:MembersItem1");
   //      LOG.info("field1Id = " + field1Id);
  //      UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
  //      LOG.info("passComponent = " + passComponent);

    
 //       if (value == null || otherValue == null) {
 //           return; // Let required="true" handle.
 //       }

        if (!value.equals(otherValue)) {
            FacesMessage msg =
                    new FacesMessage("validation error in RangeAgeValidator");
            LOG.info("validation error in RangeAgeValidator");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

} //end class
    
