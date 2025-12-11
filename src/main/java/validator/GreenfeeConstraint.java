package validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
//import jakarta.validation.Payload;
// import validator.GreenfeeValidator;

@Documented
@Constraint(validatedBy = GreenfeeValidator.class)
@Target(TYPE) // { ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

// used by TarifGreenfee.Java poour vérifier date de dénut et date de fin
public @interface GreenfeeConstraint{
    String message() default "{startdate.enddate}";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
 //   public int max() default 5; // added 1/11/2016
}