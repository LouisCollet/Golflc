package validator;

import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// see example http://softwarecave.org/2014/03/27/custom-bean-validation-constraints/
// http://stackoverflow.com/questions/19802209/how-to-apply-jsf-validator-after-annotations-constraints
// http://workingonbits.com/2011/02/28/custom-constraints-with-bean-validation/

public class GreenfeeValidator implements ConstraintValidator<GreenfeeConstraint, TarifGreenfee>{
//int max;  //added 1/11/2016

/*@Override
public void initialize(FirstUpperConstraint firstUpper){
    max = firstUpper.max();
  //  LOG.debug("max == " + max);
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}
*/
@Override
public boolean isValid(TarifGreenfee tarifGreenfee, ConstraintValidatorContext context){
     LOG.debug("entering isValid");
       return tarifGreenfee.getStartDate().isBefore(tarifGreenfee.getEndDate());
}

//    @Override
//    public boolean isValid(TarifGreenfee t, ConstraintValidatorContext cvc) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
} // end Class