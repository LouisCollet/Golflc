/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import java.util.ArrayList;
import java.util.List;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import static java.lang.System.out;
public class PassayExample { // validate
   void main() {
        
      List<Rule> rules = new ArrayList<>();
      //Rule 1: Password length should be in between 8 and 16 characters
      rules.add(new LengthRule(8, 16));
      //Rule 2: No whitespace allowed
      rules.add(new WhitespaceRule());
      //Rule 3.a: At least one Upper-case character
      rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
      //Rule 3.b: At least one Lower-case character
      rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
      //Rule 3.c: At least one digit
      rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
      //Rule 3.d: At least one special character
      rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        
      PasswordValidator validator = new PasswordValidator(rules);
      PasswordData password = new PasswordData("Microsoft123");
      RuleResult result = validator.validate(password);
        
      if(result.isValid()){
         out.println("Password validated.");
      } else {
         out.println("Invalid Password: " + validator.getMessages(result));
        }
    }

} //end class
