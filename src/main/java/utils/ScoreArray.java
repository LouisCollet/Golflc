package utils;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ScoreArrayValidator.class)
@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

public @interface ScoreArray // implements interfaces.Log
{
// custom annotation properties
    String fields = null;
    
// required by JSR-303
    
String message() default "{holes.array}";
Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};

String[] value() default {"1","2","3","4"}; // contiendra l'array avec les scores à valider (9 ou 18 strokes)
}