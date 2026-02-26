package info_test;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SessionScoped //@RequestScoped
@Named("ipInfo")
public class IpInfoBean implements Serializable {
    
    @Inject
    IpAddressDetector ipDetector;
    
    private IpAddressDetector.IpInfo ipInfo;
    private Map<String, String> details;
    
    @PostConstruct
    public void init() {
        ipInfo = ipDetector.getClientIpInfo();
        buildDetails();
    }
    
   private void buildDetails() {
        details = new HashMap<>();
        details.put("Adresse IP", ipInfo.address());
        details.put("Version", ipInfo.version().getDisplayName());
        details.put("Type de réseau", ipInfo.getNetworkType());
        details.put("Publique", ipInfo.isPublic() ? "Oui" : "Non");
        details.put("Privée", ipInfo.isPrivate() ? "Oui" : "Non");
        details.put("Loopback", ipInfo.isLoopback() ? "Oui" : "Non");
        details.put("Link-Local", ipInfo.isLinkLocal() ? "Oui" : "Non");
    }
    
    // Getters pour JSF
    
    public String getIpAddress() {
        return ipInfo.address();
    }
    
    public String getIpVersion() {
        return ipInfo.version().getDisplayName();
    }
    
    public boolean isIpV4() {
        return ipInfo.version() == IpAddressDetector.IpVersion.IPV4;
    }
    
    public boolean isIpV6() {
        return ipInfo.version() == IpAddressDetector.IpVersion.IPV6;
    }
    
    public String getNetworkType() {
        return ipInfo.getNetworkType();
    }
    
    public boolean isPublicIp() {
        return ipInfo.isPublic();
    }
    
    public String getDescription() {
        return ipInfo.getDescription();
    }
    
    public Map<String, String> getDetails() {
        return details;
    }
    
    public IpAddressDetector.IpInfo getIpInfo() {
        return ipInfo;
    }
    
    public String getIpIcon() {
        return switch (ipInfo.version()) {
            case IPV4 -> "fa-network-wired";
            case IPV6 -> "fa-globe";
            default -> "fa-question";
        };
    }
    
    public String getIpCssClass() {
        if (ipInfo.isPublic()) {
            return "ip-public";
        } else if (ipInfo.isPrivate()) {
            return "ip-private";
        } else if (ipInfo.isLoopback()) {
            return "ip-loopback";
        }
        return "ip-unknown";
    }
}