
package interfaces;


//package com.hantsylabs.example.ee8.jsf;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;

/**
 *
 * @author hantsy
 */
@Constraint(validatedBy=interfaces.PasswordValidator2.class)
@Target(TYPE)
@Retention(RUNTIME)
@interface Password {

    String message() default "Password fields must match";
    Class<Object>[] groups() default {};
    Class<Object>[] payload() default {};
}