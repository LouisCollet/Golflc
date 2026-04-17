package listeners;

import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.sql.DataSource;

@WebListener()
public class ImplHttpSessionListener implements HttpSessionListener {

    private static int activeSessions = 0;
    private static final String COUNTER = "session-counter";
    private final List<String> sessions = new ArrayList<>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        LOG.debug("sessionCreated id=" + session.getId());
        sessions.add(session.getId());
        session.setAttribute(COUNTER, this);
        LocalDateTime ldt = Instant.ofEpochMilli(session.getCreationTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LOG.debug("session created at " + ldt.format(ZDF_TIME));
        activeSessions++;
        LOG.debug("activeSessions = " + activeSessions);
    } // end method

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        LOG.debug("sessionDestroyed id=" + session.getId());
        LocalDateTime ldt = Instant.ofEpochMilli(session.getCreationTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LOG.debug("session was created at " + ldt.format(ZDF_TIME));
        LOG.debug("destroy time = " + LocalDateTime.now().format(ZDF_TIME));

        // Close audit record using auditId stored at login — cible exactement l'audit
        // de cette session (fix: l'ancienne version fermait l'audit le plus récent ouvert,
        // causant des fermetures dans le mauvais ordre en cas de sessions simultanées)
        Object auditIdObj = session.getAttribute("auditId");
        if (auditIdObj instanceof Integer auditId) {
            closeAudit(auditId);
        }

        sessions.remove(session.getId());
        session.setAttribute(COUNTER, this);
        if (activeSessions > 0) {
            activeSessions--;
        }
        LOG.debug("remaining activeSessions = " + activeSessions);
    } // end method

    /**
     * Ferme exactement l'audit identifié par auditId (stocké en session au login).
     * Uses JNDI DataSource directly (no CDI available in WebListener).
     */
    private void closeAudit(int auditId) {
        final String updateQuery = "UPDATE audit SET AuditEndDate = ? WHERE AuditId = ? AND AuditEndDate IS NULL";

        try {
            DataSource ds = (DataSource) new InitialContext().lookup("java:jboss/datasources/golflc");
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateQuery)) {

                ps.setTimestamp(1, Timestamp.from(Instant.now()));
                ps.setInt(2, auditId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    LOG.info("Audit " + auditId + " closed on session destroy");
                } else {
                    LOG.debug("Audit " + auditId + " already closed or not found");
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to close audit " + auditId + " on session destroy: " + e.getMessage());
        }
    } // end method

    public int getActiveSessions() {
        return activeSessions;
    } // end method

} // end class
