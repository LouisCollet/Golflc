
package validator;

import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;
import org.primefaces.validate.bean.ClientConstraint;

@Target({METHOD,FIELD,ANNOTATION_TYPE})
@Retention(RUNTIME)
@ClientConstraint(resolvedBy=EmailClientValidationConstraint.class)
@Documented
public @interface Email {
    String message() default "{org.primefaces.examples.primefaces}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} //end interface
