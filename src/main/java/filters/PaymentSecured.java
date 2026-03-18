package filters;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Name-binding annotation for JAX-RS payment endpoint security.
 * Endpoints annotated with @PaymentSecured are intercepted by PaymentSecurityFilter.
 * Security audit 2026-03-09
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PaymentSecured {
} // end annotation
