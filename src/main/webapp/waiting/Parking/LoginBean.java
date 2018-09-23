
package security;

import java.util.Enumeration;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

//https://www.baeldung.com/java-ee-8-security
@Named
@RequestScoped
public class LoginBean {
  @Inject
    private SecurityContext securityContext;
 
    @NotNull private String username;
 
    @NotNull private String password;
 
    public void login() {
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        Enumeration e = session.getAttributeNames();
    while (e.hasMoreElements())
    {
     String attr = (String)e.nextElement();
     System.err.println("      attr  = "+ attr);
    Object value = session.getAttribute(attr);
     System.err.println("      value = "+ value);
    }
        
        
        Credential credential = new UsernamePasswordCredential(
          username, new Password(password));
    //    AuthenticationStatus status = securityContext.authenticate(getHttpRequestFromFacesContext(),
   //         getHttpResponseFromFacesContext(),
   //         withParams().credential(credential));
        // ...
    }
      
    // ...
}
