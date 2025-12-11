/*
https://rieckpil.de/howto-simple-form-based-authentication-for-jsf-2-3-with-java-ee-8-security-api/
 */
package security;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;

/*@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login_securityAPI.xhtml",
                errorPage="",  // balusC p 416
                //The property useForwardToLogin is set to false to use a redirect instead of a forward. balusc 417
                useForwardToLogin = false
            )
)
/*
@EmbeddedIdentityStoreDefinition({
@Credentials(callerName = "Louis", password = "Collet", groups = {"user", "admin"}),
  @Credentials(callerName = "john", password = "doe", groups = {"user"}),
  @Credentials(callerName = "foo", password = "bar", groups = {"fizz", "buzz"})
})
*/
@ApplicationScoped
public class ApplicationConfig {
  public void init() {
    LOG.debug("in ApplicationConfig - init");
}
}