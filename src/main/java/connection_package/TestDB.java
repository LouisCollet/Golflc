
package connection_package;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
/*
    @Target(...) → définit où cette annotation peut être placée :
    FIELD → sur les champs (@Inject @TestDB Connection connection;)
    PARAMETER → sur les paramètres de méthodes ou constructeurs
    METHOD → sur les méthodes (utile si tu produis via @Produces)
    TYPE → sur les classes (rare pour un qualifier, mais permis)
*/
public @interface TestDB {}
