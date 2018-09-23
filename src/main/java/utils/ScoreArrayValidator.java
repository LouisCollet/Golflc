package utils;

import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ScoreArrayValidator implements ConstraintValidator<ScoreArray, Object>, interfaces.Log 
{
 private String fields;
@Override
public void initialize(ScoreArray constraint)
{
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
 //   allowedTypes = Arrays.asList(constraint.value());
    LOG.info("from initialize : " + constraint.value().toString() );
}
//private String fields;
@Override
public boolean isValid(Object value, ConstraintValidatorContext cvc)
{
	if (value == null) // || value..length == 0)
        {
		return true;
	}
return false; //value.substring(0, 1).equals(value.substring(0, 1).toUpperCase());
}

} // end Class
