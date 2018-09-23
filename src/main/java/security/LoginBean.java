
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
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
//https://github.com/rdebusscher/soteria-customform-ldap-example/blob/master/src/main/java/be/c4j/security/soteria/test/LoginBean.java 

@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage="/login_securityAPI.xhtml",
                errorPage="error_securityAPI.xhtml" // DRAFT API - must be set to empty for now
        )
)
/*
@LdapIdentityStoreDefinition(
        url = "ldap://www.zflexldap.com/",
        baseDn = "cn=ro_admin,ou=sysadmins,dc=zflexsoftware,dc=com",
        password = "zflexpass",
        searchBase = "ou=developers,dc=zflexsoftware,dc=com",
        searchExpression = "(&(uid=%s)(objectClass=person))",
        groupBaseDn = "ou=groups,ou=developers,dc=zflexsoftware,dc=com"
)
*/
@Named
@RequestScoped
public class LoginBean {

    @Inject
    private SecurityContext securityContext;

    @NotNull
    @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters")
    private String username;

    @NotNull
    @Size(min = 5, max = 50, message = "Password must be between 5 and 50 characters")
    private String password;

    public void login() {
            LOG.info("starting login of LoginBean");
        FacesContext context = FacesContext.getCurrentInstance();
        LOG.debug("line 01");
        Credential credential = new UsernamePasswordCredential(username, new Password(password));
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

    }

    public void logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = getRequest(context);
        try {
            // Logout current request
            request.logout();
            // Invalidate session so user becomes anonymous.
            request.getSession().invalidate();
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
          LOG.info("getUsername");
        return username;
    }

    public void setUsername(String username) {
        LOG.info("setUsername");
        this.username = username;
    }

    public String getPassword() {
         LOG.info("getPassword");
        return password;
    }

    public void setPassword(String password) {
           
        this.password = password;
        LOG.info("setPassword : " + this.password);
    }
} //end class