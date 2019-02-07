
package interfaces;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
/**
 *
 * @author hantsy
 */
public class PasswordValidator2 implements ConstraintValidator<Password, PasswordHolder> {

  @Override
  public void initialize(interfaces.Password constraintAnnotation) { }

  @Override
  public boolean isValid(PasswordHolder value, ConstraintValidatorContext context) {
    boolean result;
    result = value.getPassword1().equals(value.getPassword2());
    return result;
  }

} // end class