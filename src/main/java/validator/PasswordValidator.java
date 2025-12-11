
package validator;

//https://www.mkyong.com/jsf2/multi-components-validator-in-jsf-2-0/

import static interfaces.Log.LOG;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import utils.LCUtil;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator<Object>{

@Override
public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
try{
	  String password = value.toString();
           LOG.debug("password = " + password);
	  UIInput uiInputConfirmPassword = (UIInput) component.getAttributes().get("confirmPassword");
	  String confirmPassword = uiInputConfirmPassword.getSubmittedValue().toString();
            LOG.debug("confirmPassword = " + confirmPassword);
	  // Let required="true" do its job.
	  if (password == null || password.isEmpty() || confirmPassword == null
		|| confirmPassword.isEmpty()) {
			return;
	  }

	  if (!password.equals(confirmPassword)) {
		uiInputConfirmPassword.setValid(false);
                String msg =  LCUtil.prepareMessageBean("password.confirmation.notmatch");
                    throw new ValidatorException(new FacesMessage(msg));
	  }
//	}

} catch(Exception e) {
       String msg = "£££ Exception in " +  e.getMessage();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
   //    return false;
}
} // end method
} // end class