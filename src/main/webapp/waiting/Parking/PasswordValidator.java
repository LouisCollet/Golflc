/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
https://www.mkyong.com/jsf2/multi-components-validator-in-jsf-2-0/
 */
package validator;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import utils.LCUtil;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component,
		Object value) throws ValidatorException {

	  String password = value.toString();

	  UIInput uiInputConfirmPassword = (UIInput) component.getAttributes().get("confirmPassword");
	  String confirmPassword = uiInputConfirmPassword.getSubmittedValue().toString();

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