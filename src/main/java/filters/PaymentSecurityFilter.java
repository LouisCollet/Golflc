package filters;

import context.ApplicationContext;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * JAX-RS filter that secures payment REST endpoints.
 * Verifies that the incoming request has an active HTTP session
 * with an authenticated (connected) user.
 *
 * Applied only to endpoints annotated with @PaymentSecured.
 * Security audit 2026-03-09
 */
@Provider
@PaymentSecured
public class PaymentSecurityFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Inject
    private ApplicationContext appContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // 1. Check active HTTP session exists (do NOT create a new one)
        HttpSession session = servletRequest.getSession(false);
        if (session == null) {
            LOG.warn(methodName + " - payment endpoint called without active session");
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("No active session")
                            .build());
            return;
        } // end if

        // 2. Check user is authenticated via appContext
        if (appContext == null || !appContext.isConnected()) {
            LOG.warn(methodName + " - payment endpoint called by unauthenticated user, session id = " + session.getId());
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("User not authenticated")
                            .build());
            return;
        } // end if

        LOG.debug(methodName + " - payment endpoint authorized for player = " + appContext.getPlayer().getIdplayer());
    } // end method

} // end class
