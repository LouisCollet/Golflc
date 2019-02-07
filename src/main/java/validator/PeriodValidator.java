package validator;

import entite.Period;
import static interfaces.Log.LOG;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author collet
 */
public class PeriodValidator implements ConstraintValidator<PeriodConstraint, Period>{
// ne fonctionne pas !!
//@Override
//public void initialize(PeriodValidation constraint)
//{
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
//}

@Override
public boolean isValid(Period period, ConstraintValidatorContext context)
{
     LOG.info("entering is Valid");
//	if (club.getClubCity().equals("BBB") )
 //       {
 //           LOG.info("ClubCity = BBB testing error with custom messages");
 //       context.disableDefaultConstraintViolation();
 //       context.buildConstraintViolationWithTemplate("{com.mycompany.constraints.CheckCase.message}").addConstraintViolation();
 //           return false;
 //       }
        // autres tests
                
        LOG.info("Period class-validation is OK");
        return period.getStartDate().isBefore(period.getEndDate());

	//return true;
}
} // end Class
