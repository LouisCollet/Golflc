
package cache;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;


@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface InvalidateCache {

    /**
     * Liste des méthodes dont le cache doit être invalidé.
     * @Nonbinding permet d'éviter l'erreur Weld-001121
     */
    @Nonbinding
    String[] keys() default {};// invalide des clés spécifiques
    
    @Nonbinding
    boolean all() default false;  // invalide tout le cache
} //end interface