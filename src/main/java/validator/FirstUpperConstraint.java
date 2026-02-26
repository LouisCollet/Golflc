package validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = FirstUpperValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstUpperConstraint {

    String message() default "{validator.firstupper.default}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    //int max() default 5; // maximum length
}
