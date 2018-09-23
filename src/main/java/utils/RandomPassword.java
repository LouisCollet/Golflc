/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.security.SecureRandom;

/**
 *
 * @author collet
 */
public class RandomPassword implements interfaces.Log
{
private static final String NUMBERS = "0123456789";
private static final String UPPER_ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
private static final String LOWER_ALPHABETS = "abcdefghijklmnopqrstuvwxyz";
private static final String SPECIALCHARACTERS = "@#$%&*";
//private final int MINLENGTHOFPASSWORD = 8;

public String getRandomPassword(int MINLENGTHOFPASSWORD)
{
    StringBuilder password = new StringBuilder();
    int j = 0;
    for (int i = 0; i < MINLENGTHOFPASSWORD; i++) {
        password.append(getRandomPasswordCharacters(j));
        j++;
        if (j == 3)
        {
            j = 0;
        }
    }
    return password.toString();
}

private static String getRandomPasswordCharacters(int pos)
{
   // Random randomNum = new Random();

    SecureRandom randomNum = new SecureRandom() ; //((new Date()).getTime());
    StringBuilder randomChar = new StringBuilder();
    switch (pos)
    {
        case 0:
            randomChar.append(NUMBERS.charAt(randomNum.nextInt(NUMBERS.length() - 1)));
            break;
        case 1:
            randomChar.append(UPPER_ALPHABETS.charAt(randomNum.nextInt(UPPER_ALPHABETS.length() - 1)));
            break;
        case 2:
            randomChar.append(SPECIALCHARACTERS.charAt(randomNum.nextInt(SPECIALCHARACTERS.length() - 1)));
            break;
        case 3:
            randomChar.append(LOWER_ALPHABETS.charAt(randomNum.nextInt(LOWER_ALPHABETS.length() - 1)));
            break;
    }
    return randomChar.toString();

} //end method

 
} //end Class
