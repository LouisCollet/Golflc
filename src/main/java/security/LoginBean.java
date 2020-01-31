
package security;

import static interfaces.Log.LOG;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import static javax.security.auth.message.AuthStatus.FAILURE;
import javax.security.enterprise.AuthenticationStatus;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import javax.security.enterprise.SecurityContext;
import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
        LOG.info("starting LoginBean ");
    }
    
    public void login(){
        try{
            LOG.info("starting login of LoginBean");
        FacesContext context = FacesContext.getCurrentInstance();
     //   securityContext.
        LOG.debug("line 01");
        LOG.info("username = " + getUsername());
        LOG.info("password = " + getPassword());
   //     LOG.info("securitycontext = " + securityContext.getCallerPrincipal().getName());
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
        LOG.info("status = " + status);
        if (status.equals(SEND_CONTINUE)) {
            // Authentication mechanism has send a redirect, should not
            // send anything to response from JSF now.
            context.responseComplete();
        } else if (status.equals(FAILURE) || status.equals(SEND_FAILURE)) {
            // TODO: only 1 status should be returned. Change to enum or fix source.
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
            LOG.info("request.logout was called");
            // Invalidate session so user becomes anonymous.
            request.getSession().invalidate();
             LOG.info("request.getSession was invalidated");
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
          LOG.info("getUsername = " + username);
        return username;
    }

    public void setUsername(String username) {
        LOG.info("setUsername = " + username);
        this.username = username;
    }

    public String getPassword() {
         LOG.info("getPassword = " + password);
        return password;
    }

    public void setPassword(String password) {
           
        this.password = password;
        LOG.info("setPassword : " + this.password);
    }
} //end class