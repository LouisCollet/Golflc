package session;
// ==================== ISSUE REPORT SERVICE (FAKE) ====================

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class IssueReportService implements Serializable {
    
  //  private static final Logger logger = Logger.getLogger(IssueReportService.class.getName());
    
    /**
     * Soumet un rapport d'incident (fake - log seulement pour le moment)
     */
    public void submit(IssueReport report) {
        // Pour le moment, on log juste les informations
        LOG.debug("=== FAKE ISSUE REPORT SUBMITTED ===");
        LOG.debug("Type: " + report.getType());
        LOG.debug("Description: " + report.getDescription());
        LOG.debug("Session ID: " + report.getSessionId());
        LOG.debug("IP Address: " + report.getIpAddress());
        LOG.debug("User Agent: " + report.getUserAgent());
        LOG.debug("Last Access Time: " + report.getLastAccessTime());
        LOG.debug("====================================");
        
        // Simuler un succès
        // Dans la vraie version, on sauvegarderait en base de données
    }
    
    /**
     * Met à jour le statut d'un rapport (fake pour le moment)
     */
    public void updateStatus(Long reportId, String newStatus, String notes) {
        LOG.debug(String.format("FAKE: Update report #%d to status %s", reportId, newStatus));
        // Dans la vraie version, on mettrait à jour en base de données
    }
    
    /**
     * Retourne les rapports de l'utilisateur (fake - liste vide pour le moment)
     */
    public List<IssueReport> getUserReports() {
        LOG.debug("FAKE: Getting user reports - returning empty list");
        return new ArrayList<>();
    }
    
    /**
     * Retourne les rapports par statut (fake - liste vide pour le moment)
     */
    public List<IssueReport> getReportsByStatus(String status) {
        LOG.debug("FAKE: Getting reports by status: " + status + " - returning empty list");
        return new ArrayList<>();
    }
    
    /**
     * Compte les rapports de l'utilisateur (fake - retourne 0 pour le moment)
     */
    public long getUserReportCount() {
        LOG.debug("FAKE: Getting user report count - returning 0");
        return 0L;
    }
}

