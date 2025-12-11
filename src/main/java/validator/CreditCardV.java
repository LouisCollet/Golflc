package validator;

import static interfaces.Log.LOG;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented

@Target( { ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreditCardValidator.class)
// used by Creditcard.Java pour vérifier si le numéro de la carte de crédit est valide
public @interface CreditCardV{
    
//    LOG.debug("we are now in CreditCardV");
    String message() default "{creditcard.invalidnumber}for ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    public int max() default 16; // added 1/11/2016

}