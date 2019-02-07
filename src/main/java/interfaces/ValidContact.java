/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import org.primefaces.validate.bean.ClientConstraint;

@Documented
@Constraint(validatedBy = {ContactValidator.class})
@ClientConstraint(resolvedBy=ValidContactClientConstraint.class)
@Target({ANNOTATION_TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValidContact {
   String message() default "Invalid contact !";
   Class<?>[] groups() default {};
   Class<? extends Payload>[] payload() default {};
}