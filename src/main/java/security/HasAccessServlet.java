/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.IOException;
import jakarta.annotation.security.DeclareRoles;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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