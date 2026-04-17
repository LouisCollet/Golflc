package listeners;

import static interfaces.Log.LOG;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import javax.naming.InitialContext;
import javax.sql.DataSource;

@WebListener
public class StartStopAppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        LOG.debug("Application deployed on the server - Servlet Context Initialized ... " + this);
        try {
            ServletContext context = event.getServletContext();
            context.setAttribute("playerid", "");
            context.setAttribute("playerlastname", "");
            context.setAttribute("playerage", 0);
            context.setAttribute("creditcardType", "INITIALIZED");
            context.setAttribute("deploymentTime", System.currentTimeMillis());
            LOG.debug("Server info = " + context.getServerInfo());

            // Close all orphaned audits from previous server run
            closeOrphanedAudits();

        } catch (Exception ex) {
            LOG.error("Exception in contextInitialized " + ex);
        }
    } // end method

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOG.debug("Application undeployed from the server - Servlet Context Destroyed");
    } // end method

    /**
     * Close all audit records with auditEndDate IS NULL.
     * At application startup, all open audits are orphans from the previous run.
     */
    private void closeOrphanedAudits() {
        final String query = "UPDATE audit SET auditEndDate = ? WHERE auditEndDate IS NULL";
        try {
            DataSource ds = (DataSource) new InitialContext().lookup("java:jboss/datasources/golflc");
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setTimestamp(1, Timestamp.from(Instant.now()));
                int rows = ps.executeUpdate();
                LOG.info("Application startup — closed " + rows + " orphaned audit(s)");
            }
        } catch (Exception e) {
            LOG.error("Failed to close orphaned audits at startup: " + e.getMessage());
        }
    } // end method

} // end class
