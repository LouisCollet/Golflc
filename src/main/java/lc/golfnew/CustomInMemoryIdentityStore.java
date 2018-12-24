
package lc.golfnew;

import static interfaces.Log.LOG;
import java.util.Arrays;
import java.util.HashSet;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;


@ApplicationScoped
public class CustomInMemoryIdentityStore implements IdentityStore {

    public CredentialValidationResult validate(Credential credential) {
      LOG.info("entering validate");
        UsernamePasswordCredential login = (UsernamePasswordCredential) credential;
      LOG.info("login.getCaller() = " + login.getCaller());
      LOG.info("login.getPasswordAsString() = " + login.getPasswordAsString());
      
        if (login.getCaller().equals("admin@mail.com") 
                       && login.getPasswordAsString().equals("ADMIN1234")) {
            LOG.info("line 01");
            return new CredentialValidationResult("admin", new HashSet<>(Arrays.asList("ADMIN")));
        } else if (login.getCaller().equals("user@mail.com") 
                       && login.getPasswordAsString().equals("USER1234")) {
            LOG.info("line 02");
            return new CredentialValidationResult("user", new HashSet<>(Arrays.asList("USER")));
        } else {
            LOG.info("line 03");
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
    }
} // end class
