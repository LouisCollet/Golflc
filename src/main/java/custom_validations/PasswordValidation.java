/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package custom_validations;

import static interfaces.Log.LOG;

/**
 *
 * @author Collet
 */
//https://stackoverflow.com/questions/3802192/regexp-java-for-password-validation 
public class PasswordValidation {
    public void main(String[] args) {
   //   String passwd = "aaZZa44@"; 
        String passwd = "aaZZ a44@"; 
      LOG.info("password to be validated = " + passwd);
 //     String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
   //   String pattern = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}\\z";
      String pattern = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z";
    //  LOG.info(passwd.matches(pattern).toString().toUpperCase());
      LOG.info("Résultat des courses = " + Boolean.toString(passwd.matches(pattern)).toUpperCase());
   }
}
/*
Explanations:

(?=.*[0-9]) a digit must occur at least once
(?=.*[a-z]) a lower case letter must occur at least once
(?=.*[A-Z]) an upper case letter must occur at least once
(?=.*[@#$%^&+=]) a special character must occur at least once
(?=\\S+$) no whitespace allowed in the entire string
.{8,} at least 8 character
*/
