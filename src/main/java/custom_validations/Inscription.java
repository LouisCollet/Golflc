package custom_validations;

import static java.lang.annotation.ElementType.*; //ANNOTATION_TYPE;
//import static java.lang.annotation.ElementType.CONSTRUCTOR;
//import static java.lang.annotation.ElementType.FIELD;
//import static java.lang.annotation.ElementType.METHOD;
//import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = InscriptionValidator.class)
@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)

public @interface Inscription
{
String message() default "{club.name.uppercase}";

Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};
// new 19/08/2014
String value() default "";
int round() default 0;

}
