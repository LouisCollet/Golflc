
package validator;

//https://www.mkyong.com/jsf2/multi-components-validator-in-jsf-2-0/

import static interfaces.Log.LOG;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import utils.LCUtil;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator{

@Override
public void validate(FacesContext context, UIComponent component,	Object value) throws ValidatorException {

	  String password = value.toString();
           LOG.info("password = " + password);
	  UIInput uiInputConfirmPassword = (UIInput) component.getAttributes().get("confirmPassword");
	  String confirmPassword = uiInputConfirmPassword.getSubmittedValue().toString();
            LOG.info("confirmPassword = " + confirmPassword);
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
	}
} // end class