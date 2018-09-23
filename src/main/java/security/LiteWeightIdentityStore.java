
package security;

import static interfaces.Log.LOG;
import static java.util.Arrays.asList;
import java.util.HashSet;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import javax.security.enterprise.identitystore.IdentityStore;

//https://www.ibm.com/developerworks/library/j-javaee8-security-api-1/index.html

@ApplicationScoped // The @ApplicationScope annotation ensures thatthe instance is a CDI-managed bean,
                   // which is available to the entireapplication.
public class LiteWeightIdentityStore implements IdentityStore {

   public CredentialValidationResult validate(UsernamePasswordCredential userCredential) {
            LOG.info("entering LiteWeightIdentityStore - CredentialValidationResult");
       if (userCredential.compareTo("admin", "pwd1")) {
           return new CredentialValidationResult("admin", new HashSet<>(asList("admin", "user", "demo")));
       }
       return INVALID_RESULT;
   }
} // end class

/*Designing your own lightweight identity store is quite simple.
You arerequired to implement the IdentityStore interface and atleast the validate() method. 
There are four methods on theinterface, all of which have default method implementations. 
The validate() method is the minimum required for a workingidentity store. 
It accepts an instance of Credential andreturns an instance of CredentialValidationResults.

In Listing 6, the validate() method receives an instance of UsernamePasswordCredential containing login credentials tovalidate.
It then returns an instance of CredentialValidationResults.
If the simple configurationlogic results in a successful authentication,
this object is configuredwith the username and a set of groups to which the user belongs. 
If authentication fails, then the CredentialValidationResultsinstance contains only the status flag INVALID.
*/