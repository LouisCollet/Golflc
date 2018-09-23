package custom_validations;

import java.util.Scanner;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// see example http://softwarecave.org/2014/03/27/custom-bean-validation-constraints/
// http://stackoverflow.com/questions/19802209/how-to-apply-jsf-validator-after-annotations-constraints
// http://workingonbits.com/2011/02/28/custom-constraints-with-bean-validation/

public class CreditCardValidator implements ConstraintValidator<CreditCardV, String>, interfaces.Log
{
int max;  //added 1/11/2016

@Override
public void initialize(CreditCardV firstUpper)
{
    max = firstUpper.max();
    LOG.info("CreditCard Validator with input max = " + max);
	// See JSR 303 Section 2.4.1 for sample implementation.
    // gives access to any attributes of the annotation such min/max fields of the size annotation
}

@Override
public boolean isValid(String value, ConstraintValidatorContext context)
{
    LOG.info("entering isValid");
    LOG.info("entering isValid with value = " + value);
    	if (value == null || value.length() == 0)
        {
            LOG.info(" validateCreditCardNumber : card NUMBER is null");
	return false;
	}
    value = value.replaceAll(" ", "");
    LOG.info("entering isValid with zero removed = " + value);//return value.substring(0, 1).equals(value.substring(0, 1).toUpperCase());
    return validateCreditCardNumber(value);
}

private boolean validateCreditCardNumber(String str) {
    LOG.info("entering validateCreditCardNumber");
    LOG.info("entering validateCreditCardNumber with input = " + str);
		int[] ints = new int[str.length()];
		for (int i = 0; i < str.length(); i++) {
			ints[i] = Integer.parseInt(str.substring(i, i + 1));
		}
                
		for (int i = ints.length - 2; i >= 0; i = i - 2) {
			int j = ints[i];
			j = j * 2;
			if (j > 9) {
				j = j % 10 + 1;
			}
			ints[i] = j;
		}
		int sum = 0;
		for (int i = 0; i < ints.length; i++) {
			sum += ints[i];
		}
		if (sum % 10 == 0) {
                    LOG.info(" validateCreditCardNumber : card OK");
			return true;
		} else {
                     LOG.info(" validateCreditCardNumber : card INVALID NUMBER");
			return false;
		}
	} //end method

    public static void main(final String args[])
    {
        String cType = null;

        System.out.println("Enter a credit card number: ");
        final Scanner input = new Scanner(System.in);
        final String cardNumber = input.next();

        if (cardNumber.startsWith("4"))
        {
            cType = "Visa";
        }
        else if (cardNumber.startsWith("5"))
        {
            cType =  "MasterCard";
        }
        else if (cardNumber.startsWith("6"))
        {
            cType =  "Discover";
        }
        else if (cardNumber.startsWith("37"))
        {
            cType =  "American Express";
        }
        else
        {
            cType =  "Unknown type";
        }

   //     final long total = sumOfEvenPlaces(Long.valueOf(cardNumber)) + (sumOfOddPlaces(Long.valueOf(cardNumber)) * 2);

//        if (isValid(total))
 //       {
 //           System.out.println("The " + cType + " card number is valid");
  //      }
  //      else
  //      {
  //          System.out.println("The " + cType + " card number is invalid.");
  //      }
    }

public int luhnCardValidator(int cardNumbers[]) {
                int sum = 0, nxtDigit=0;
                for (int i = 0; i<cardNumbers.length; i++) {
                    if (i % 2 == 0) 
                      nxtDigit  = (nxtDigit > 4) ? (nxtDigit * 2 - 10) + 1 : nxtDigit * 2;
                    sum += nxtDigit;
                }
                return (sum % 10);
            }
public int[] longToIntArray(long cardNumber){

return Long.toString(cardNumber).chars()
    .map(x -> x - '0') //converts char to int 
    .toArray();  //converts to int array
}






} // end Class