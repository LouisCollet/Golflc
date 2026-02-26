
package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import static interfaces.Log.LOG;
import jakarta.inject.Named;

/**
 * Classe de test pour vérifier que IpDetector fonctionne
 */
@ApplicationScoped
@Named("ipDetectorTest")

public class IpDetectorTest {
    
    @Inject
    HttpServletRequest request;
    
    /**
     * Test simple pour vérifier que tout compile
     */
    public void testIpDetection() {
        // Appel de la méthode statique
        String ip = IpDetector.getClientIp(request);
        
        LOG.info("IP détectée: {}", ip);
        
        // Test des autres méthodes
        if (IpDetector.isIpv4(ip)) {
            LOG.info("C'est une IPv4");
        } else if (IpDetector.isIpv6(ip)) {
            LOG.info("C'est une IPv6");
        }
        
        // Version
        int version = IpDetector.getIpVersion(ip);
        LOG.info("Version IP: {}", version);
        
        // Type
        boolean isPublic = IpDetector.isPublicIp(ip);
        LOG.info("IP publique: {}", isPublic);
        
        // Description
        String description = IpDetector.getIpDescription(ip);
        LOG.info("Description: {}", description);
    }
    public String getIpDescription() {
        String ip = IpDetector.getClientIp(request);
        LOG.info("IP détectée: {}", ip);
        // Description
        String description = IpDetector.getIpDescription(ip);
        LOG.info("Description: {}", description);
        return description;
    }
}