package validator;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import javax.validation.Payload;

@Documented
//// @Constraint(validatedBy = MatchplayValidator.class)
@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

public @interface Matchplay
{
//String message() default "{club.name.uppercase}";
String message() default "message from Matchplay.java validator";

Class<?>[] groups() default {};
Class<? extends Payload>[] payload() default {};

//Class[] groups() default {};
//Class[] payload() default {};

//String departement();
}