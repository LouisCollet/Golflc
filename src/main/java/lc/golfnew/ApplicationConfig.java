
package lc.golfnew;

//import static interfaces.Log.LOG;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

/**
 *@CustomFormAuthenticationMechanismDefinition(
  loginToContinue = @LoginToContinue(loginPage = "/login.xhtml"))

}

 * @author Baeldung
 * https://www.baeldung.com/java-ee-8-security
 */
@CustomFormAuthenticationMechanismDefinition(
  loginToContinue = @LoginToContinue(
    loginPage = "/login_securityAPI.xhtml") 
)//,
 //   errorPage = "/error.xhtml"))
@ApplicationScoped
//@Named
public class ApplicationConfig implements interfaces.Log{

 public void init(){
  LOG.info("entering ApplicationConfig");
    }
   } // end class