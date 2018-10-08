package validator;

import static interfaces.Log.LOG;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = CreditCardValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

// used by Club.Java poour vérifier si la première lettre du club est une majuscule
public @interface CreditCardV
{
//    LOG.info("we are now in CreditCardV");
    String message() default "{creditcard.invalidnumber}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    public int max() default 16; // added 1/11/2016

}