package lc.golfnew;

import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
/**
 *
 * The backing bean for the login page is responsible for validating
 * the incoming credentials and redirects to the secured area if the credentials are valid.
 * To use our custom IdentityStore the backing beans injects the SecurityContext
 * and passes the username and password to our in-memory authentication.
 * With the bean validation annotations we get a pre-validation 
 * (e.g. check the password against custom password rules) of the user input out of the box:
 */

//https://rieckpil.de/howto-simple-form-based-authentication-for-jsf-2-3-with-java-ee-8-security-api/
@Named("loginBacking")
@SessionScoped
//@SessionScoped
public class Login implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;

@NotNull(message="user name cannot be null louis")
@Size(min=3, max= 5, message = "Username must be between {min} and {max} characters")
    private String username;

//@NotNull(message="{password.notnull}")
@Size(min=5, max=50, message = "Password must be between {min} and {max} characters")
    private String password;

@Inject
    private SecurityContext securityContext;
@Inject
    private ExternalContext externalContext;
@Inject
    private FacesContext facesContext;

    public void submit() throws IOException {
        LOG.info("entering Login.submit");
        switch (continueAuthentication()) {
            case SEND_CONTINUE:
                 LOG.info("case SEND_CONTINUE");
                facesContext.responseComplete();
                break;
            case SEND_FAILURE:
                LOG.info("case SEND_FAILURE");
                utils.LCUtil.showMessageFatal("LoginAPI - login failed !");
                break;
            case SUCCESS:
                LOG.info("case SUCCESS");
                utils.LCUtil.showMessageInfo("LoginAPI - login succeed !");
    //Entering valid credentials will redirect the user to the secured index.html which will ...
                    LOG.info("context path = " + externalContext.getRequestContextPath());
                externalContext.redirect(externalContext.getRequestContextPath() + "/app/index.xhtml");
                break;
            case NOT_DONE:
                LOG.info("this is the case not done");
                break;
            default:
                 LOG.info("this is the case default");
        }
    }

    private AuthenticationStatus continueAuthentication() {
        LOG.info("entering continueAuthentification");
        return securityContext.authenticate(
              (HttpServletRequest) externalContext.getRequest(),
              (HttpServletResponse) externalContext.getResponse(),
               AuthenticationParameters.withParams()
                  .credential(new UsernamePasswordCredential(username, password))
        );
    }
    /*
    // from https://github.com/eugenp/tutorials/blob/master/java-ee-8-security-api/app-auth-custom-form-store-custom/src/main/java/com/baeldung/javaee/security/LoginBean.java
private HttpServletRequest getHttpRequestFromFacesContext() { 
         return (HttpServletRequest) facesContext 
                  .getExternalContext() 
                 .getRequest(); 
     } 
 
     private HttpServletResponse getHttpResponseFromFacesContext() { 
          return (HttpServletResponse) facesContext 
                  .getExternalContext() 
                  .getResponse(); 
     } 
*/
   // getters & setters

    public String getUsername() {
         LOG.info("getUsername = " + username);
        return username;
    }

    public void setUsername(String username) {
        
        this.username = username;
     LOG.info("setUsername = " + this.username);
    }


    public String getPassword() {
        LOG.info("get password = " + password);
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        LOG.info("set password = " + this.getPassword());
    }
} // end class
