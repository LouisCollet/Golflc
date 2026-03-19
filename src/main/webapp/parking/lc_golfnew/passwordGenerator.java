
package lc.golfnew;

import static interfaces.Log.LOG;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.inject.Named;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;


@Named("passwordG") // this qualifier  makes a bean EL-injectable (Expression Language)
//@SessionScoped
public class passwordGenerator {
private static String generatePassword() {
      List<CharacterRule> rules = new ArrayList<>();
      //Rule 1:  At least one UpperCase
      rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
      //Rule 2.c: At least four LowerCase
      rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 4));
      //Rule 3.c: At least two Digit
      rules.add(new CharacterRule(EnglishCharacterData.Digit, 2));
      //Rule 3.d: At least one Special character
      rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
	final PasswordGenerator generator = new PasswordGenerator();
	// Generated password is 8 characters long, which complies with policy
	return generator.generatePassword(8, rules);
}

public static String generateRandomPassword(int len, int randNumOrigin, int randNumBound){
    /*https://www.techiedelight.com/generate-random-alphanumeric-password-java/
		SecureRandom random = new SecureRandom();
		return random.ints(randNumOrigin, randNumBound + 1)
				.filter(i -> Character.isUpperCase(i))
				.limit(len)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint,StringBuilder::append)
				.toString();
        */        
        SecureRandom random = new SecureRandom();
        return random.ints(len, randNumOrigin, randNumBound + 1)
                    .mapToObj(i -> String.valueOf((char)i))
                    .collect(Collectors.joining());
                
                
	}

   void main() {
   String p= generatePassword();
       LOG.debug("password generated 1 = " + p);
       int len = 6;
  //For example, to generate a password with all lower case letters set the range as 97-122 (ASCII value of 'a'-'z').
  //   int randNumOrigin = 48, randNumBound = 122;
      int randNumOrigin = 65, randNumBound = 90; // uppercase
     LOG.debug("password generated 2 = " + generateRandomPassword(len, randNumOrigin, randNumBound));    
   }

    } // end class