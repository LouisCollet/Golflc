/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import static interfaces.Log.LOG;
import java.util.Arrays;
import java.util.HashSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
// https://rieckpil.de/howto-simple-form-based-authentication-for-jsf-2-3-with-java-ee-8-security-api/
@ApplicationScoped
public class CustomInMemoryIdentityStore implements IdentityStore {
    @Override
    public CredentialValidationResult validate(Credential credential) {
        LOG.info("entering CustomInMemoryIdentityStore vamidate with credential = " + credential);
        UsernamePasswordCredential login = (UsernamePasswordCredential) credential;
 
        if (login.getCaller().equals("admin@mail.com") 
                       && login.getPasswordAsString().equals("ADMIN1234")) {
            return new CredentialValidationResult("admin", new HashSet<>(Arrays.asList("ADMIN")));
        } else if (login.getCaller().equals("user@mail.com") 
                       && login.getPasswordAsString().equals("USER1234")) {
            return new CredentialValidationResult("user", new HashSet<>(Arrays.asList("USER")));
        } else {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
    }
}