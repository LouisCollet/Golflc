
package session;

// ==================== ISSUE REPORT SERVICE (FAKE) ====================

import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;


@ApplicationScoped
public class IssueReportService implements Serializable {
    
   // private static final Logger logger = Logger.getLogger(IssueReportService.class.getName());
    
    /**
     * Soumet un rapport d'incident (fake - log seulement pour le moment)
     */
    public void submit(IssueReport report) {
        // Pour le moment, on log juste les informations
        logger.info("=== FAKE ISSUE REPORT SUBMITTED ===");
        logger.info("Type: " + report.getType());
        logger.info("Description: " + report.getDescription());
        logger.info("Session ID: " + report.getSessionId());
        logger.info("IP Address: " + report.getIpAddress());
        logger.info("User Agent: " + report.getUserAgent());
        logger.info("Last Access Time: " + report.getLastAccessTime());
        logger.info("====================================");
        
        // Simuler un succès
        // Dans la vraie version, on sauvegarderait en base de données
    }
    
    /**
     * Met à jour le statut d'un rapport (fake pour le moment)
     */
    public void updateStatus(Long reportId, String newStatus, String notes) {
        logger.info(String.format("FAKE: Update report #%d to status %s", reportId, newStatus));
        // Dans la vraie version, on mettrait à jour en base de données
    }
    
    /**
     * Retourne les rapports de l'utilisateur (fake - liste vide pour le moment)
     */
    public List<IssueReport> getUserReports() {
        logger.info("FAKE: Getting user reports - returning empty list");
        return new ArrayList<>();
    }
    
    /**
     * Retourne les rapports par statut (fake - liste vide pour le moment)
     */
    public List<IssueReport> getReportsByStatus(String status) {
        logger.info("FAKE: Getting reports by status: " + status + " - returning empty list");
        return new ArrayList<>();
    }
    
    /**
     * Compte les rapports de l'utilisateur (fake - retourne 0 pour le moment)
     */
    public long getUserReportCount() {
        logger.info("FAKE: Getting user report count - returning 0");
        return 0L;
    }
}

// ==================== ISSUE REPORT (DTO SIMPLE) ====================

public class IssueReport implements Serializable {
    private Long id;
    private String type;
    private String description;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private Date lastAccessTime;
    private Date createdAt;
    
    public IssueReport() {
        this.createdAt = new Date();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public Date getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(Date lastAccessTime) { 
        this.lastAccessTime = lastAccessTime; 
    }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
