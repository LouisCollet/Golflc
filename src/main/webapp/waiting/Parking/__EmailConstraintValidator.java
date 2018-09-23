/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package custom_validations;

/**
 *
 * @author collet
 */
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
 
/**
 
 * ConstraintValidator for Email Input
 
 * ,, a  quoo ca sert ????
 
public class EmailConstraintValidator implements ConstraintValidator<Email, String>{

    private Pattern pattern;

        private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
    public void initialize(Email a) {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }
 
    public boolean isValid(String value, ConstraintValidatorContext cvc) {
        if(value == null)
            return true;
        else
           return pattern.matcher(value.toString()).matches();
    }
} *///end class 