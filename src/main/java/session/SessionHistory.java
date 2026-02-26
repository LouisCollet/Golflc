
package session;

import java.io.Serializable;
import java.time.LocalDateTime;
// Un record est immutable → plus de setters
// Les getters sont générés automatiquement (id(), username(), etc.)
public record SessionHistory(
        Long id,
        String username,
        LocalDateTime loginTime,
        String device,
        String browser,
        String ipAddress,
        Integer duration,
        String logoutReason
) implements Serializable {

    // Constructeur secondaire équivalent à ton ancien constructeur pourquoi ?
    public SessionHistory(
            Long id,
            String username,
            LocalDateTime loginTime,
            String device,
            String browser,
            String ipAddress
    ) {
        this(id, username, loginTime, device, browser, ipAddress, null, null);
    }
}




/*
import java.io.Serializable;

public class SessionHistory implements Serializable {
    private Long id;
    private String username;
    private Date loginTime;
    private String device;
    private String browser;
    private String ipAddress;
    private Integer duration;
    private String logoutReason;
    
    public SessionHistory(Long id, String username, Date loginTime, String device, 
                         String browser, String ipAddress) {
        this.id = id;
        this.username = username;
        this.loginTime = loginTime;
        this.device = device;
        this.browser = browser;
        this.ipAddress = ipAddress;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Date getLoginTime() { return loginTime; }
    public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
    
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public String getLogoutReason() { return logoutReason; }
    public void setLogoutReason(String logoutReason) { this.logoutReason = logoutReason; }
}
*/