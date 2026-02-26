
package info_test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static interfaces.Log.LOG;

/**
 * Service de détection d'adresse IP avec support IPv4 et IPv6.
 * Gère les proxies, load balancers et différentes configurations réseau.
 */
@RequestScoped
@Named
public class IpAddressDetector {
    
    // Pattern pour validation IPv4
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );
    
    // Pattern pour validation IPv6
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
        "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,7}:$|" +
        "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|" +
        "^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|" +
        "^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|" +
        "^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$|" +
        "^:((:[0-9a-fA-F]{1,4}){1,7}|:)$"
    );
    
    // Headers à vérifier pour l'IP (par ordre de priorité)
    private static final List<String> IP_HEADERS = Arrays.asList(
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_FORWARDED_FOR",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED",
        "X-Cluster-Client-IP"
    );
    
    /**
     * Obtient l'adresse IP du client avec détection automatique IPv4/IPv6
     * 
     * @return L'adresse IP détectée ou "unknown"
     */
    public String getClientIp() {
        try {
            HttpServletRequest request = getHttpRequest();
            if (request == null) {
                LOG.warn("HttpServletRequest is null");
                return "unknown";
            }
            
            // Essayer d'abord les headers (proxies/load balancers)
            Optional<String> ipFromHeaders = extractIpFromHeaders(request);
            if (ipFromHeaders.isPresent()) {
                return ipFromHeaders.get();
            }
            
            // Sinon, utiliser l'adresse distante
            String remoteAddr = request.getRemoteAddr();
            return remoteAddr != null && !remoteAddr.isBlank() ? remoteAddr : "unknown";
            
        } catch (Exception e) {
            LOG.error("Error detecting client IP", e);
            return "unknown";
        }
    }
    
    /**
     * Obtient les informations détaillées sur l'IP du client
     * 
     * @return IpInfo contenant toutes les informations
     */
    public IpInfo getClientIpInfo() {
        String ip = getClientIp();
        
        if ("unknown".equals(ip)) {
            return new IpInfo(ip, IpVersion.UNKNOWN, false, false, false);
        }
        
        try {
            InetAddress addr = InetAddress.getByName(ip);
            IpVersion version = detectVersion(addr);
            boolean isPrivate = isPrivateAddress(addr);
            boolean isLoopback = addr.isLoopbackAddress();
            boolean isLinkLocal = addr.isLinkLocalAddress();
            
            return new IpInfo(ip, version, isPrivate, isLoopback, isLinkLocal);
            
        } catch (UnknownHostException e) {
            LOG.warn("Cannot parse IP address: {}", ip);
            return new IpInfo(ip, IpVersion.UNKNOWN, false, false, false);
        }
    }
    
    /**
     * Vérifie si l'IP du client est IPv4
     */
    public boolean isClientIpV4() {
        return getClientIpInfo().version() == IpVersion.IPV4;
    }
    
    /**
     * Vérifie si l'IP du client est IPv6
     */
    public boolean isClientIpV6() {
        return getClientIpInfo().version() == IpVersion.IPV6;
    }
    
    /**
     * Obtient l'IP en forçant IPv4 (retourne null si IPv6)
     */
    public String getClientIpV4Only() {
        IpInfo info = getClientIpInfo();
        return info.version() == IpVersion.IPV4 ? info.address() : null;
    }
    
    /**
     * Obtient l'IP en forçant IPv6 (retourne null si IPv4)
     */
    public String getClientIpV6Only() {
        IpInfo info = getClientIpInfo();
        return info.version() == IpVersion.IPV6 ? info.address() : null;
    }
    
    /**
     * Obtient l'IP avec préfixe de version (ex: "IPv4: 192.168.1.1")
     */
    public String getClientIpWithVersion() {
        IpInfo info = getClientIpInfo();
        return info.version().name() + ": " + info.address();
    }
    
    /**
     * Vérifie si l'IP est privée (RFC 1918 pour IPv4, ULA pour IPv6)
     */
    public boolean isClientIpPrivate() {
        return getClientIpInfo().isPrivate();
    }
    
    /**
     * Extrait l'IP depuis les headers HTTP
     */
    private Optional<String> extractIpFromHeaders(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For peut contenir plusieurs IPs séparées par des virgules
                String[] ips = value.split(",");
                for (String ip : ips) {
                    String trimmedIp = ip.trim();
                    if (isValidIpAddress(trimmedIp)) {
                        LOG.debug("IP found in header {}: {}", header, trimmedIp);
                        return Optional.of(trimmedIp);
                    }
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Valide qu'une chaîne est une adresse IP valide (IPv4 ou IPv6)
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        
        // Nettoyer l'IP (enlever les espaces, les ports, etc.)
        String cleanIp = cleanIpAddress(ip);
        
        return IPV4_PATTERN.matcher(cleanIp).matches() || 
               IPV6_PATTERN.matcher(cleanIp).matches();
    }
    
    /**
     * Nettoie l'adresse IP (enlève les ports, les crochets IPv6, etc.)
     */
    private String cleanIpAddress(String ip) {
        // Enlever les crochets pour IPv6 (ex: [2001:db8::1]:8080 -> 2001:db8::1)
        if (ip.startsWith("[") && ip.contains("]:")) {
            int endBracket = ip.indexOf("]:");
            return ip.substring(1, endBracket);
        }
        
        // Enlever le port pour IPv4 (ex: 192.168.1.1:8080 -> 192.168.1.1)
        if (ip.contains(":") && !ip.contains("::")) {
            String[] parts = ip.split(":");
            if (parts.length == 2 && IPV4_PATTERN.matcher(parts[0]).matches()) {
                return parts[0];
            }
        }
        
        return ip;
    }
    
    /**
     * Détecte la version IP (IPv4 ou IPv6)
     */
    private IpVersion detectVersion(InetAddress addr) {
        if (addr instanceof Inet4Address) {
            return IpVersion.IPV4;
        } else if (addr instanceof Inet6Address) {
            return IpVersion.IPV6;
        }
        return IpVersion.UNKNOWN;
    }
    
    /**
     * Vérifie si une adresse est privée
     */
    private boolean isPrivateAddress(InetAddress addr) {
        return addr.isSiteLocalAddress() || 
               addr.isLinkLocalAddress() || 
               addr.isLoopbackAddress() ||
               isPrivateIPv6(addr);
    }
    
    /**
     * Vérifie si une adresse IPv6 est privée (ULA - Unique Local Address)
     */
    private boolean isPrivateIPv6(InetAddress addr) {
        if (!(addr instanceof Inet6Address)) {
            return false;
        }
        
        byte[] address = addr.getAddress();
        // ULA commence par fc00::/7 (fc00 ou fd00)
        return (address[0] & 0xfe) == 0xfc;
    }
    
    /**
     * Obtient la requête HTTP depuis le contexte JSF ou CDI
     */
    private HttpServletRequest getHttpRequest() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            return (HttpServletRequest) facesContext.getExternalContext().getRequest();
        }
        return null;
    }
    
    /**
     * Énumération des versions IP
     */
    public enum IpVersion {
        IPV4("IPv4"),
        IPV6("IPv6"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        
        IpVersion(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Record contenant les informations complètes sur une IP
     */
    public record IpInfo(
        String address,
        IpVersion version,
        boolean isPrivate,
        boolean isLoopback,
        boolean isLinkLocal
    ) {
        /**
         * Obtient une description textuelle de l'IP
         */
        public String getDescription() {
            StringBuilder desc = new StringBuilder();
            desc.append(version.getDisplayName()).append(" address: ").append(address);
            
            if (isLoopback) {
                desc.append(" (loopback)");
            } else if (isLinkLocal) {
                desc.append(" (link-local)");
            } else if (isPrivate) {
                desc.append(" (private)");
            } else {
                desc.append(" (public)");
            }
            
            return desc.toString();
        }
        
        /**
         * Vérifie si c'est une IP publique
         */
        public boolean isPublic() {
            return !isPrivate && !isLoopback && !isLinkLocal;
        }
        
        /**
         * Obtient le type de réseau
         */
        public String getNetworkType() {
            if (isLoopback) return "Loopback";
            if (isLinkLocal) return "Link-Local";
            if (isPrivate) return "Private";
            return "Public";
        }
    }
}