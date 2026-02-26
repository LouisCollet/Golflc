package session;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


// ==================== SESSION BEAN (UTILISATION DES SERVICES FAKE) ====================

@Named("sessionBean")
@ViewScoped
public class SessionBean implements Serializable {
    
    @Inject
    private HttpServletRequest request;
    
    @Inject
    private SessionHistoryService sessionHistoryService;
    
    @Inject
    private IssueReportService issueReportService;
    
    private String issueType;
    private String issueDescription;
    private boolean includeSessionInfo = true;
    
    // ===== Méthodes utilitaires =====
    
    public String getUserAgentShort() {
        String ua = request.getHeader("user-agent");
        if (ua == null) return "Unknown";
        
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Edge")) return "Edge";
        
        return ua.length() > 50 ? ua.substring(0, 50) + "..." : ua;
    }
    
    public String formatInactiveInterval(int seconds) {
        int minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minutes";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return hours + "h " + remainingMinutes + "min";
    }
    
    // ===== Méthodes utilisant les services fake =====
    
    public List<SessionHistory> getRecentSessions() {
        return sessionHistoryService.getRecentSessions(5);
    }
    
    public String viewFullHistory() {
        return "sessionHistory.xhtml?faces-redirect=true";
    }
    
    public void submitIssueReport() {
        try {
            IssueReport report = new IssueReport();
            report.setType(issueType);
            report.setDescription(issueDescription);
            report.setUserAgent(request.getHeader("user-agent"));
            report.setIpAddress(request.getRemoteAddr());
            
            if (includeSessionInfo) {
                report.setSessionId(request.getSession().getId());
                long timestamp = request.getSession().getLastAccessedTime();
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                report.setLastAccessTime(dateTime);
            }
            
            issueReportService.submit(report);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Succès", 
                    "Votre rapport a été envoyé avec succès (mode test)"));
                    
            // Reset form
            issueType = null;
            issueDescription = null;
            includeSessionInfo = true;
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", 
                    "Impossible d'envoyer le rapport: " + e.getMessage()));
        }
    }
    
    // ===== Getters & Setters =====
    
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { 
        this.issueDescription = issueDescription; 
    }
    
    public boolean isIncludeSessionInfo() { return includeSessionInfo; }
    public void setIncludeSessionInfo(boolean includeSessionInfo) { 
        this.includeSessionInfo = includeSessionInfo; 
    }
}
