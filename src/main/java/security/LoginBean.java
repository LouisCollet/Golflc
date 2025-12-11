
package security;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import static jakarta.faces.application.FacesMessage.SEVERITY_ERROR;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import static jakarta.security.auth.message.AuthStatus.FAILURE;
import jakarta.security.enterprise.AuthenticationStatus;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import jakarta.security.enterprise.SecurityContext;
import static jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import utils.LCUtil;
//https://github.com/rdebusscher/soteria-customform-ldap-example/blob/master/src/main/java/be/c4j/security/soteria/test/LoginBean.java 



@Named("loginBean")
@RequestScoped
public class LoginBean {

    @Inject
    private SecurityContext securityContext;

    @NotNull
    @Size(min=3, max=15, message = "Username must be between {min} and {max} characters")
    private String username;

    @NotNull(message="{password.notnull}")
    @Size(min=5, max=50, message = "Password must be between {min} and {max} characters")
    private String password;

    /*
    public void submit() throws IOException {

        // credential that want to be validate was UsernamePasswordCredential
        Credential credential = new UsernamePasswordCredential(username, new Password(password));

        // this will call our security configuration to authorize the user
        AuthenticationStatus status = this.securityContext.authenticate(
                getRequest(),
                getResponse(),
                withParams()
                        .credential(credential)
                        .newAuthentication(!loginToContinue)
                        .rememberMe(remember)
        );

        if (status.equals(SUCCESS)) {

            redirect("index.xhtml");

        } else if (status.equals(SEND_FAILURE)) {

            addGlobalError("auth.message.error.failure");
            validationFailed();

        }
}
    */
    public void start(){
        LOG.debug("starting LoginBean ");
    }
    
    public void login(){
        try{
            LOG.debug("starting login of LoginBean");
        FacesContext context = FacesContext.getCurrentInstance();
     //   securityContext.
        LOG.debug("line 01");
        LOG.debug("username = " + getUsername());
        LOG.debug("password = " + getPassword());
   //     LOG.debug("securitycontext = " + securityContext.getCallerPrincipal().getName());
    //    Credential credential = new UsernamePasswordCredential(username, new Password(password));
     //   Credential credential = new UsernamePasswordCredential(username,password);
        Credential credential = new UsernamePasswordCredential(username, new Password(password));
   //     Credential cr = new UsernamePasswordCredential();
        
        LOG.debug("line 02 credential = " + credential);
        // Request for authentication
        AuthenticationStatus status = securityContext.authenticate(
                getRequest(context),
                getResponse(context),
                withParams().credential(credential));
        LOG.debug("status = " + status);
        if (status.equals(SEND_CONTINUE)) {
            // Authentication mechanism has send a redirect, should not
            // send anything to response from JSF now.
            context.responseComplete();
        } else if (status.equals(FAILURE) || status.equals(SEND_FAILURE)) {
            // TO DO: only 1 status should be returned. Change to enum or fix source.
            addError(context, "Authentication failed");
        }
 } catch (Exception e){
            String msg = "££ Exception in login = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
     }
    }

    public void logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = getRequest(context);
        try {
            // Logout current request
            request.logout();
            LOG.debug("request.logout was called");
            // Invalidate session so user becomes anonymous.
            request.getSession().invalidate();
             LOG.debug("request.getSession was invalidated");
        } catch (ServletException e) {
            addError(context, e.getMessage());
        }

    }

    private static HttpServletResponse getResponse(FacesContext context) {
        return (HttpServletResponse) context
                .getExternalContext()
                .getResponse();
    }

    private static HttpServletRequest getRequest(FacesContext context) {
        return (HttpServletRequest) context
                .getExternalContext()
                .getRequest();
    }

    private static void addError(FacesContext context, String message) {
        context.addMessage(null,
                new FacesMessage(SEVERITY_ERROR, message, null));
    }

    public String getUsername() {
          LOG.debug("getUsername = " + username);
        return username;
    }

    public void setUsername(String username) {
        LOG.debug("setUsername = " + username);
        this.username = username;
    }

    public String getPassword() {
         LOG.debug("getPassword = " + password);
        return password;
    }

    public void setPassword(String password) {
           
        this.password = password;
        LOG.debug("setPassword : " + this.password);
    }
} //end class