package validator;

import entite.Club;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ClubValidator_1 implements ConstraintValidator<ClubValidation, Club>, interfaces.Log
{
// ne fonctionne pas !!
@Override
public void initialize(ClubValidation constraint)
{
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}

@Override
public boolean isValid(Club club, ConstraintValidatorContext context){
	if (club.getAddress().getCity().equals("BBB") )
        {
            LOG.debug("ClubCity = BBB testing error with custom messages");
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{com.mycompany.constraints.CheckCase.message}").addConstraintViolation();
            return false;
        }
        // autres tests
        LOG.debug("Club class-validation is OK");
	return true;
}
} // end Class
