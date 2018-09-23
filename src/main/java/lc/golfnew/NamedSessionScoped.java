/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;

/**
 *
 * @author collet
 */
//https://www.packtpub.com/books/content/contexts-and-dependency-injection-netbeans
// We annotated the StereotypeClient class with our NamedSessionScoped stereotype,
// which is equivalent to using the @Named and @SessionScoped annotations.
@Named
@SessionScoped
@Stereotype
@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
public @interface NamedSessionScoped {
}
