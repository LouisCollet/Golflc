
package custom_validations;

import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator(value="dateValidator")
public class __DateValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIInput sd = (UIInput)component.getAttributes().get("firstDate");
        Date firstDate = (Date)sd.getValue();
        Date secondDate = (Date)value;
        if(!firstDate.before(secondDate)){
            FacesMessage msg = new FacesMessage("Entered dates are invalid: first date must be before second date");
            throw new ValidatorException(msg);
        }
    }

}