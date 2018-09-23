/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.IOException;
import javax.annotation.security.DeclareRoles;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@DeclareRoles({"admin", "user", "demo"})
@WebServlet("/hasAccessServlet")
public class HasAccessServlet extends HttpServlet {
   @Inject
   private SecurityContext securityContext;
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       boolean hasAccess = securityContext.hasAccessToWebResource("/secretServlet", "GET");
       if (hasAccess){
           req.getRequestDispatcher("/secretServlet").forward(req, res);
       } else {
           req.getRequestDispatcher("/logout").forward(req, res);
       }
   }
} // end class