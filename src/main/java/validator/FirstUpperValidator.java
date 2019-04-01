package validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// see example http://softwarecave.org/2014/03/27/custom-bean-validation-constraints/
// http://stackoverflow.com/questions/19802209/how-to-apply-jsf-validator-after-annotations-constraints
// http://workingonbits.com/2011/02/28/custom-constraints-with-bean-validation/

public class FirstUpperValidator implements ConstraintValidator<FirstUpper, String>, interfaces.Log{
int max;  //added 1/11/2016

@Override
public void initialize(FirstUpper firstUpper)
{
    max = firstUpper.max();
  //  LOG.info("max == " + max);
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}

@Override
public boolean isValid(String value, ConstraintValidatorContext context){
	if (value == null || value.length() == 0)
        {
		return true;
	}
return value.substring(0, 1).equals(value.substring(0, 1).toUpperCase());
}

} // end Class