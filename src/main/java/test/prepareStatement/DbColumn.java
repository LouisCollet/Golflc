
package test.prepareStatement;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbColumn {
    boolean ignore() default false;
    String defaultValue() default "";
}
