package session;

import java.io.Serializable;
import java.time.LocalDateTime;

// ==================== ISSUE REPORT (DTO SIMPLE) ====================
/*
public record IssueReport(
        Long id,
        String type,
        String description,
        String sessionId,
        String ipAddress,
        String userAgent,
        LocalDateTime lastAccessTime,
        LocalDateTime createdAt
) implements Serializable {

    // Constructeur sans arguments équivalent à ton ancien constructeur
    public IssueReport() {
        this(null, null, null, null, null, null, null, LocalDateTime.now());
    }
}
*/
// ==================== ISSUE REPORT (DTO SIMPLE) ====================

public class IssueReport implements Serializable {
    private Long id;
    private String type;
    private String description;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastAccessTime;
    private LocalDateTime createdAt;
    
    public IssueReport() {
//        this.createdAt = new LocalDateTime();
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
    
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { 
        this.lastAccessTime = lastAccessTime; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}