package validator;

import entite.Cotisation;
import static interfaces.Log.LOG;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class ContactValidator implements ConstraintValidator<ValidContact, Cotisation> {

   @Override
   public void initialize(ValidContact constraintAnnotation) {
      // NOOP
   }

   @Override
   public boolean isValid(Cotisation cotisation, ConstraintValidatorContext context) {
       LOG.debug("entering isValid from ContactValidator");
       if(cotisation.getCotisationEndDate().isBefore(cotisation.getCotisationStartDate())){
           LOG.debug("KO : ContactValidator enddate is before startdate");
           return false;
       }else{
           LOG.debug("OK : ContactValidator enddate is after startdate");
           return true;
       }
//      return value.getEmail().startsWith(value.getName());
   }
}
