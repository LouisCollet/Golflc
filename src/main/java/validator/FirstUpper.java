package validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = FirstUpperValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

// used by Club.Java poour vérifier si la première lettre du club est une majuscule
public @interface FirstUpper
{
    String message() default "{club.name.uppercase}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    public int max() default 5; // added 1/11/2016
}