
package security;

import static interfaces.Log.LOG;
import static java.util.Arrays.asList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;

//https://www.ibm.com/developerworks/library/j-javaee8-security-api-1/index.html

@BasicAuthenticationMechanismDefinition(realmName = "golflc")

@DatabaseIdentityStoreDefinition(
  dataSourceLookup = "${'java:jboss/datasources/golflc'}",
  callerQuery = "#{'select PlayerPassword from player where idplayer = ?'}",
  groupsQuery = "select GROUPNAME from groups where username = ?",
  priority=30)

@ApplicationScoped // The @ApplicationScope annotation ensures that the instance is a CDI-managed bean,
                   // which is available to the entire application.
public class CustomIdentityStore implements IdentityStore {
    
@Inject
   private IdentityStoreHandler idStoreHandler;

   public CredentialValidationResult validate(UsernamePasswordCredential userCredential) {
            LOG.info("entering CustomIdentityStore - CredentialValidationResult");
            LOG.info("userCredential = " + userCredential);
       if (userCredential.compareTo("adminLC", "pwd1")) {
           return new CredentialValidationResult("adminLC", new HashSet<>(asList("admin", "user", "demo")));
       }else{
           return INVALID_RESULT;
       }
   } //end method
   
   // determines the ordre of iteration if multiple stores have been implemented
   @Override
    public int priority() {
        return 70;
    }
    
  //  By default, an IdentityStore processes both credentials validation (ValidationType.VALIDATE)
    //and group retrieval(ValidationType.PROVIDE_GROUPS).
    //We can override this behavior so that it can provide only one capability.
   //Thus, we can configure the IdentityStore to be used only for credentials validation
    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(ValidationType.VALIDATE);
    }

} // end class

/*Designing your own lightweight identity store is quite simple.
You are required to implement the IdentityStore interface and at least the validate() method. 
There are four methods on the interface, all of which have default method implementations. 
The validate() method is the minimum required for a working identity store. 
It accepts an instance of Credential and returns an instance of CredentialValidationResults.

In Listing 6, the validate() method receives an instance of UsernamePasswordCredential containing login credentials to validate.
It then returns an instance of CredentialValidationResults.
If the simple configuration logic results in a successful authentication,
this object is configured with the username and a set of groups to which the user belongs. 
If authentication fails, then the CredentialValidationResultsinstance contains only the status flag INVALID.
*/