
package lc.golfnew;

import static interfaces.Log.LOG;
import java.util.Arrays;
import java.util.HashSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;


@ApplicationScoped
public class CustomInMemoryIdentityStore implements IdentityStore{

    public CredentialValidationResult validate(Credential credential) {
      LOG.debug("entering validate");
        UsernamePasswordCredential login = (UsernamePasswordCredential) credential;
      LOG.debug("login.getCaller() = " + login.getCaller());
      LOG.debug("login.getPasswordAsString() = " + login.getPasswordAsString());
      
    if (login.getCaller().equals("admin@mail.com") && login.getPasswordAsString().equals("ADMIN1234")) {
            LOG.debug("line 01");
            return new CredentialValidationResult("admin", new HashSet<>(Arrays.asList("ADMIN")));
    } else if (login.getCaller().equals("user@mail.com") && login.getPasswordAsString().equals("USER1234")) {
            LOG.debug("line 02");
            return new CredentialValidationResult("user", new HashSet<>(Arrays.asList("USER")));
        } else {
            LOG.debug("line 03");
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
    }
} // end class