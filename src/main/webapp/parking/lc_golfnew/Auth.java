
package lc.golfnew;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.SessionMap;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import static utils.LCUtil.showMessageFatal;

@Named
@SessionScoped
public class Auth implements java.io.Serializable{

    private String username;
    private String password;
    private String originalURL;
   
@Inject
private ExternalContext ec;
@PostConstruct
    public void init() {
        LOG.debug("entering Auth.init");
///        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        originalURL = (String) ec.getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);
  //      LOG.debug("line 01");
        if (originalURL == null) {
            originalURL = ec.getRequestContextPath() + "/welcome.xhtml";
                LOG.debug("originalURL, was null  = " + originalURL);
        }else{
            String originalQuery = (String) ec.getRequestMap().get(RequestDispatcher.FORWARD_QUERY_STRING);
                LOG.debug("originalQuery = " + originalQuery);
            if (originalQuery != null) {
                originalURL += "?" + originalQuery;
                    LOG.debug("originalURL = " + originalURL);
            }
        }
 } // end method init
//@EJB

@SessionMap
private Map<String, Object> sessionMapJSF23;
public void login() throws IOException {
   //     FacesContext context = FacesContext.getCurrentInstance();
   //     ExternalContext externalContext = context.getExternalContext();
     LOG.debug("entering Auth.login()");
  HttpServletRequest request = (HttpServletRequest) ec.getRequest();

try {
        LOG.debug("username Balus = " + username);
        LOG.debug("password Balus = " + password);
    request.login(username, password);
       //     User user = userService.find(username, password);
    Player player = new Player();
    player.setPlayerLastName("Collet");
    sessionMapJSF23.put("user", player.getPlayerLastName());
     //       ec.getSessionMap().put("user", player.getPlayerLastName());
    ec.redirect(originalURL);
}catch(ServletException e) {
            // Handle unknown username/password in request.login().
            String msg = "Unknown login Balus = " + e;
            LOG.error(msg);
            showMessageFatal(msg);
        }
} // end method login

    public void logout() throws IOException {
    //    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
              LOG.debug("entering Auth.logout()");
      ec.invalidateSession();
      ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
    }

    // Getters/setters for username and password.

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        LOG.debug("setuserename = " + username);
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }
}