
package validator;

import entite.Player;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

//non utilisé https://github.com/eldermoraes/javaee8-cookbook/blob/master/chapter01/ch01-jsf/src/main/java/com/eldermoraes/ch01/jsf/UserValidator.java
// 

@FacesValidator("playerValidator")
public class PlayerValidator implements Validator<Player> {

    @Override
    public void validate(FacesContext fc, UIComponent uic, Player player) throws ValidatorException {
        if(!player.getPlayerEmail().contains("@")){
            throw new ValidatorException(new FacesMessage(null, "Player - Malformed e-mail"));
        }
    }
    
}
