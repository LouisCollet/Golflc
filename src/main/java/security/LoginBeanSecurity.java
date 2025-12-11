package security;

import java.io.IOException;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import jakarta.security.enterprise.SecurityContext;
import static jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
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
@RequestScoped
public class LoginBeanSecurity implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;
@Inject  private SecurityContext securityContext;
@Inject  private ExternalContext externalContext;
@Inject  private FacesContext facesContext;

@NotNull(message="user name cannot be null louis")
@Size(min=3, max= 5, message = "Username must be between {min} and {max} characters")
    private String username;

//@NotNull(message="{password.notnull}")
@Size(min=5, max=50, message = "Password must be between {min} and {max} characters")
    private String password;
//FacesContext facesContext = FacesContext.getCurrentInstance();
 //    LOG.debug("facesContext = " + facesContext);
//ExternalContext eco = fc.getExternalContext();

//ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
//    

    public void submit() throws IOException {
        LOG.debug("entering Login.submit");
        LOG.debug("with facesContext PhaseId= " + facesContext.getCurrentPhaseId());
        LOG.debug("with facesContext getAttributes = " + facesContext.getAttributes().toString());
        switch(continueAuthentication()) {
            case SEND_CONTINUE:
                 LOG.debug("case SEND_CONTINUE");
                facesContext.responseComplete();
                break;
            case SEND_FAILURE:
                LOG.debug("case SEND_FAILURE");
                utils.LCUtil.showMessageFatal("LoginAPI - login failed !");
                break;
            case SUCCESS:
                LOG.debug("case SUCCESS");
                utils.LCUtil.showMessageInfo("LoginAPI - login succeed !");
    //Entering valid credentials will redirect the user to the secured index.html which will ...
                    LOG.debug("context path = " + externalContext.getRequestContextPath());
                externalContext.redirect(externalContext.getRequestContextPath() + "/app/index.xhtml");
                break;
            case NOT_DONE:
                LOG.debug("this is the case not done");
                break;
            default:
                 LOG.debug("this is the case default");
        }
    }

    private AuthenticationStatus continueAuthentication() {
 try{
        LOG.debug("entering continueAuthentification");
        LOG.debug("username = " + username);
        LOG.debug("password = " + password);
  //      LOG.debug("external context = " + context.getContextName());
        LOG.debug("security context = " + securityContext.toString());
   //     LOG.debug("security context caller principal = " + securityContext..getCallerPrincipal();
//        FacesContext fc = FacesContext.getCurrentInstance();
//ExternalContext eco = fc.getExternalContext();
 //LOG.debug("external context eco = " + eco.toString());
 //LOG.debug("external context get request = " + eco.getRequest());
 Credential credential = new UsernamePasswordCredential(username, new Password(password));
    LOG.debug("credential = " + credential.toString());
    LOG.debug("credential isValid = " + credential.isValid());
// LOG.debug(getHttpRequestFromFacesContext());
AuthenticationStatus status = 
        securityContext.authenticate(
              getRequest(),
              getResponse(),
              withParams().credential(credential)
        );
LOG.debug("authentication status = " + status);
 return SEND_CONTINUE;  // pour continuer tests
//return status;
   }catch (Exception e){
	LOG.error("Fatal Exception in continue Authentication "  + e);
}       return null;
    }
    
    // from https://github.com/eugenp/tutorials/blob/master/java-ee-8-security-api/app-auth-custom-form-store-custom/src/main/java/com/baeldung/javaee/security/LoginBean.java
private HttpServletRequest getRequest() {
    HttpServletRequest sr = 
          (HttpServletRequest) facesContext 
                 .getExternalContext() 
                 .getRequest(); 
    LOG.debug("servlet request path = " + sr.getServletPath());
    return sr;
     } 
 
     private HttpServletResponse getResponse() { 
       HttpServletResponse  srep = 
          (HttpServletResponse) facesContext 
                  .getExternalContext() 
                  .getResponse(); 
       LOG.debug("servlet response = " + srep);
          return srep;
     } 

    public String getUsername() {
  //       LOG.debug("getUsername = " + username);
        return username;
    }

    public void setUsername(String username) {
        
        this.username = username;
 //    LOG.debug("setUsername = " + this.username);
    }


    public String getPassword() {
 //       LOG.debug("get password = " + password);
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
  //      LOG.debug("set password = " + this.getPassword());
    }
} // end class