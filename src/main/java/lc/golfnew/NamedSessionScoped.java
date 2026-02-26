/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Named;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

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
@Retention((RetentionPolicy.RUNTIME))
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface NamedSessionScoped {
}
