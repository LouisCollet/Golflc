package custom_validations;

/**
 *
 * @author collet
 */
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ClubValidator.class)
@Target(TYPE)  // annotation for a class
//@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)

public @interface ClubValidation
{
//String message() default "{club.class.validation}";
String message() default "error in ClubValidator";
// required by validation runtime
Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};
} //end Class