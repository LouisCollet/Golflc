
package validator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = {ContactValidator.class})
//https://beginnersbook.com/2014/09/java-annotations/
@Documented // @Documented annotation indicates that elements using this 
            //annotation should be documented by JavaDoc. 
//@Target(TYPE)
//@Target
//It specifies where we can use the annotation. For example:
//In the below code, we have defined the target type as METHOD which means the below 
//annotation can only be used on methods.
// ligne suivante : on les prend tous pour être sûr ?  ElementType.CONSTRUCTOR existe aussi
@Target( { ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
//@Retention
//It indicates how long annotations with the annotated type are to be retained.
public @interface ValidContact {
   String message() default "Valid Cotisation errors between StartDate and EndDate";
   Class<?>[] groups() default {};
   Class<? extends Payload>[] payload() default {};
}