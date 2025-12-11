
package security;

// import jakarta.annotation.security.DeclareRoles;
//import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * https://javaee.github.io/j1-hol/ex-security.html
 */
//@WebServlet(name = "NewServlet", urlPatterns = {"/NewServlet"})
@WebServlet(name = "NewServlet", urlPatterns = {"/test"})
// @DeclareRoles({"foo", "bar"}) // specify that our application will work with 2 types of user roles: foo & bar.
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo")) //Servlet container on HTTP protocol messages, i.e. only user of role “foo” will be allowed.
//@BasicAuthenticationMechanismDefinition(realmName="HOL-basic" ) //The realmName pass in parameter will be used in the WWW-Authenticate header.
// @EmbeddedIdentityStoreDefinition({ // specify which IdentityStore to use,
//    @Credentials(callerName = "david", password = "david", groups = {"foo"}),
//    @Credentials(callerName = "ed", password = "ed", groups = {"bar",}),
//    @Credentials(callerName = "michael", password = "michael", groups = {"foo"})}
// )

public class NewServlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    try (PrintWriter out = response.getWriter()) {
        out.println("<!DOCTYPE html><html><body>");
        out.println("<div style=\"font-size:150%;font-weight:100;font-family: sans-serif;"); 
        out.println("text-align: center;color: DimGray;margin-top: 40vh;line-height: 150%\">");
        out.println("Java EE 8 HoL<br/>");
        out.println(request.getAuthType());
        out.println("</div></body></html>");
    }
 }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
  @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
} // end class