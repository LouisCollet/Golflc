
package interfaces;


//package com.hantsylabs.example.ee8.jsf;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
//import validator.PasswordValidator;

/**
 *
 * @author hantsy
 */
@Constraint(validatedBy=interfaces.PasswordValidator2.class)
@Target(TYPE)
@Retention(RUNTIME)
@interface Password {

    String message() default "Password fields must match";
    Class[] groups() default {};
    Class[] payload() default {};
}