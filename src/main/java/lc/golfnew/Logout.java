/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped //automatic CDI injection when requested
public class Logout {
 
    /**
     * Shiro logout for the current user http://are-you-ready.de/blog/2017/05/06/apache-shiro-part-2-securing-a-jsf-java-ee-7-application/
     */
    public void submit() throws IOException {
   //     if (SecurityUtils.getSubject().hasRole("root")) {
   //         final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
   //         SecurityUtils.getSubject().logout();
  //          externalContext.invalidateSession();  // cleanup user related session state
   //         externalContext.redirect("login.xhtml?faces-redirect=truelogin.xhtml");
   //     }
    }
}