package validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;

@Documented
@Constraint(validatedBy = PeriodValidator.class)
@Target(TYPE)  // annotation for a class
//@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface PeriodConstraint{
//String message() default "{club.class.validation}";
String message() default "Start date must be before end date";
// required by validation runtime
Class<?>[] groups() default {};
//Class<? extends Payload>[] payload() default {};
Class<?> [] payload() default {};
} //end Class