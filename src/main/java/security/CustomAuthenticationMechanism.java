/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Collet
 */
@ApplicationScoped
public class CustomAuthenticationMechanism implements HttpAuthenticationMechanism {
 
   @Inject
   private IdentityStoreHandler idStoreHandler;
 
   @Override
   public AuthenticationStatus validateRequest(HttpServletRequest req, 
                                               HttpServletResponse res, 
                                               HttpMessageContext msg) {
       // use idStoreHandler to authenticate and authorize access
       return msg.responseUnauthorized(); // other responses available
   }
}

