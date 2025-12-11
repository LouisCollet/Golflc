
package security;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/welcome")
@ServletSecurity(@HttpConstraint(rolesAllowed = "USER_ROLE"))
public class WelcomeServlet extends HttpServlet {

    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        securityContext.hasAccessToWebResource("/protectedServlet", "GET");
        resp.getWriter().write("" +
                "Authentication type :" + req.getAuthType() + "\n" +
                "Caller Principal :" + securityContext.getCallerPrincipal() + "\n" +
                "User in Role USER_ROLE :" + securityContext.isCallerInRole("USER_ROLE") + "\n" +
                "User in Role ADMIN_ROLE :" + securityContext.isCallerInRole("ADMIN_ROLE") + "\n" +
                "");
    }
}
