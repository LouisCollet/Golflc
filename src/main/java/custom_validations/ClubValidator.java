package custom_validations;

import entite.Club;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author collet
 */
public class ClubValidator implements ConstraintValidator<ClubValidation, Club>, interfaces.Log
{
// ne fonctionne pas !!
@Override
public void initialize(ClubValidation constraint)
{
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}

@Override
public boolean isValid(Club club, ConstraintValidatorContext context)
{
	if (club.getClubCity().equals("BBB") )
        {
            LOG.info("ClubCity = BBB testing error with custom messages");
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{com.mycompany.constraints.CheckCase.message}").addConstraintViolation();
            return false;
        }
        // autres tests
        LOG.info("Club class-validation is OK");
	return true;
}
} // end Class
