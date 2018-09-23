package custom_validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// see example http://softwarecave.org/2014/03/27/custom-bean-validation-constraints/

public class InscriptionValidator implements ConstraintValidator<FirstUpper, String>, interfaces.Log
{

@Override
public void initialize(FirstUpper firstUpper)
{
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}

@Override
public boolean isValid(String value, ConstraintValidatorContext context)
{
	if (value == null || value.length() == 0)
        {
		return true;
	}
return value.substring(0, 1).equals(value.substring(0, 1).toUpperCase());
}

} // end Class