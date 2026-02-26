package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static interfaces.Log.LOG;
import jakarta.inject.Named;

/**
 * Détecteur d'adresse IP simple - fonctionne partout (JAX-RS, Servlets, JSF, etc.)
 * Usage: IpDetector.getClientIp(request) ou injection CDI
 */
@ApplicationScoped
@Named("ipDetector")
public class IpDetector {
    
    /**
     * Obtient l'IP du client depuis une requête HTTP
     * Méthode statique utilisable partout
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            LOG.debug("HttpServletRequest request = null");
            return "HttpServletRequest request = null";
        }
        
        // 1. Vérifier X-Forwarded-For (proxy/load balancer)
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Prendre la première IP de la liste
            String ip = xff.split(",")[0].trim();
            if (!ip.isBlank()) {
                LOG.debug("ip is not blank");
                return ip;
            }
        }
        
        // 2. Vérifier X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            LOG.debug("xRealIp");
            return xRealIp;
        }
        
        // 3. Utiliser l'adresse distante
        String remoteAddr = request.getRemoteAddr();
        LOG.debug("retmoteaddr = " + remoteAddr);
        return remoteAddr != null ? remoteAddr : "unknown";
    }
    
    /**
     * Vérifie si une IP est IPv4
     */
    public static boolean isIpv4(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            LOG.debug("InetAddress addr = " + addr);
            return addr instanceof Inet4Address;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si une IP est IPv6
     */
    public static boolean isIpv6(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            LOG.debug("isIpv6 ?");
            return addr instanceof Inet6Address;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtient la version IP (4 ou 6)
     */
    public static int getIpVersion(String ip) {
        if (isIpv4(ip)) return 4;
        if (isIpv6(ip)) return 6;
        return 0;
    }
    
    /**
     * Vérifie si une IP est privée
     */
    public static boolean isPrivateIp(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            LOG.debug("isPrivateIp ?");
            return addr.isSiteLocalAddress() || 
                   addr.isLoopbackAddress() || 
                   addr.isLinkLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si une IP est publique
     */
    public static boolean isPublicIp(String ip) {
        LOG.debug("isPublicIp ?");
        return !isPrivateIp(ip) && !ip.equals("unknown");
    }
    
    /**
     * Obtient une description de l'IP
     */
    public static String getIpDescription(String ip) {
        if ("unknown".equals(ip)) {
            LOG.debug("IP inconnue");
            return "IP inconnue";
        }
        
        int version = getIpVersion(ip);
        String type = isPublicIp(ip) ? "publique" : "privée";
        return String.format("IPv%d %s: %s", version, type, ip);
    }
}