package info_test;

import info_test.IpAddressDetector;
import info_test.IpAddressDetector.IpInfo;
import info_test.IpAddressDetector.IpVersion;
import jakarta.annotation.security.DenyAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import static interfaces.Log.LOG;

/**
 * Exemples d'utilisation du IpAddressDetector
 */
public class IpDetectorExamples {
    
    @Inject
    IpAddressDetector ipDetector;
    
    // ========================================================================
    // EXEMPLE 1: Usage basique - Obtenir l'IP
    // ========================================================================
    
    public void example1_BasicUsage() {
        // Obtenir simplement l'IP
        String ip = ipDetector.getClientIp();
        LOG.info("Client IP: {}", ip);
        
        // Obtenir l'IP avec la version
        String ipWithVersion = ipDetector.getClientIpWithVersion();
        LOG.info("IP with version: {}", ipWithVersion);
        // Résultat: "IPv4: 192.168.1.100" ou "IPv6: 2001:db8::1"
    }
    
    // ========================================================================
    // EXEMPLE 2: Vérifier la version IP
    // ========================================================================
    
    public void example2_CheckIpVersion() {
        if (ipDetector.isClientIpV4()) {
            LOG.info("Client utilise IPv4");
            String ipv4 = ipDetector.getClientIpV4Only();
            LOG.info("IPv4 address: {}", ipv4);
        } else if (ipDetector.isClientIpV6()) {
            LOG.info("Client utilise IPv6");
            String ipv6 = ipDetector.getClientIpV6Only();
            LOG.info("IPv6 address: {}", ipv6);
        }
    }
    
    // ========================================================================
    // EXEMPLE 3: Obtenir les informations détaillées
    // ========================================================================
    
    public void example3_DetailedInfo() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        LOG.info("=== IP Information ===");
        LOG.info("Address: {}", info.address());
        LOG.info("Version: {}", info.version());
        LOG.info("Is Private: {}", info.isPrivate());
        LOG.info("Is Public: {}", info.isPublic());
        LOG.info("Is Loopback: {}", info.isLoopback());
        LOG.info("Is Link-Local: {}", info.isLinkLocal());
        LOG.info("Network Type: {}", info.getNetworkType());
        LOG.info("Description: {}", info.getDescription());
    }
    
    // ========================================================================
    // EXEMPLE 4: Traitement conditionnel selon le type d'IP
    // ========================================================================
    
    public void example4_ConditionalProcessing() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        if (info.isPublic()) {
            // Traitement pour IP publique
            LOG.info("Public IP detected, applying security rules");
            applyPublicIpSecurityRules(info.address());
        } else if (info.isPrivate()) {
            // Traitement pour IP privée
            LOG.info("Private IP detected, internal network access");
            grantInternalAccess();
        } else if (info.isLoopback()) {
            // Traitement pour localhost
            LOG.info("Loopback detected, development mode");
            enableDevelopmentFeatures();
        }
    }
    
    // ========================================================================
    // EXEMPLE 5: Logging avec distinction IPv4/IPv6
    // ========================================================================
    
    public void example5_LoggingWithVersion() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        String logMessage = String.format(
            "User connected from %s address %s (%s network)",
            info.version().getDisplayName(),
            info.address(),
            info.getNetworkType()
        );
        
        LOG.info(logMessage);
        // Résultat: "User connected from IPv4 address 192.168.1.100 (Private network)"
        // ou: "User connected from IPv6 address 2001:db8::1 (Public network)"
    }
    
    // ========================================================================
    // EXEMPLE 6: Restriction d'accès basée sur l'IP
    // ========================================================================
    
    public boolean example6_AccessControl() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        // Autoriser uniquement les IP publiques IPv4
        if (info.version() != IpVersion.IPV4) {
            LOG.warn("Access denied: Only IPv4 is supported");
            return false;
        }
        
        if (!info.isPublic()) {
            LOG.warn("Access denied: Only public IPs are allowed");
            return false;
        }
        
        LOG.info("Access granted for public IPv4: {}", info.address());
        return true;
    }
    
    // ========================================================================
    // EXEMPLE 7: REST API retournant les informations IP
    // ========================================================================
    
    @Path("/api/ip")
    @DenyAll // security audit 2026-03-09 — info/debug endpoints disabled in production
    public static class IpInfoResource {
        
        @Inject
        IpAddressDetector ipDetector;
        
        @GET
        @Path("/info")
        @Produces(MediaType.APPLICATION_JSON)
        public IpInfoDTO getIpInfo() {
            IpInfo info = ipDetector.getClientIpInfo();
            return new IpInfoDTO(
                info.address(),
                info.version().name(),
                info.isPublic(),
                info.isPrivate(),
                info.getNetworkType()
            );
        }
        
        @GET
        @Path("/version")
        @Produces(MediaType.TEXT_PLAIN)
        public String getIpVersion() {
            return ipDetector.getClientIpInfo().version().getDisplayName();
        }
        
        @GET
        @Path("/address")
        @Produces(MediaType.TEXT_PLAIN)
        public String getIpAddress() {
            return ipDetector.getClientIp();
        }
    }
    
    // ========================================================================
    // EXEMPLE 8: Audit avec distinction IPv4/IPv6
    // ========================================================================
    
    public void example8_AuditLogging(String userId, String action) {
        IpInfo info = ipDetector.getClientIpInfo();
        
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setIpAddress(info.address());
        log.setIpVersion(info.version().name());
        log.setNetworkType(info.getNetworkType());
        log.setTimestamp(java.time.LocalDateTime.now());
        
        // Sauvegarder en base de données
        saveAuditLog(log);
        
        LOG.info("Audit: User {} performed {} from {} ({})", 
                 userId, action, info.address(), info.version());
    }
    
    // ========================================================================
    // EXEMPLE 9: Statistiques par version IP
    // ========================================================================
    
    public void example9_IpVersionStatistics() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        // Incrémenter les compteurs selon la version
        if (info.version() == IpVersion.IPV4) {
            incrementCounter("ipv4_connections");
        } else if (info.version() == IpVersion.IPV6) {
            incrementCounter("ipv6_connections");
        }
        
        // Logger pour analyse
        LOG.info("Connection from {} - Total IPv4: {}, Total IPv6: {}", 
                 info.version(), 
                 getCounter("ipv4_connections"),
                 getCounter("ipv6_connections"));
    }
    
    // ========================================================================
    // EXEMPLE 10: Géolocalisation avec prise en compte de la version IP
    // ========================================================================
    
    public void example10_GeoLocationWithIpVersion() {
        IpInfo info = ipDetector.getClientIpInfo();
        
        // Certaines bases GeoIP ont des données différentes pour IPv4 vs IPv6
        if (info.isPublic()) {
            if (info.version() == IpVersion.IPV4) {
                // Utiliser la base GeoIP IPv4
                String location = lookupLocationIPv4(info.address());
                LOG.info("IPv4 location: {}", location);
            } else if (info.version() == IpVersion.IPV6) {
                // Utiliser la base GeoIP IPv6
                String location = lookupLocationIPv6(info.address());
                LOG.info("IPv6 location: {}", location);
            }
        } else {
            LOG.info("Private IP, no geolocation available");
        }
    }
    
    // Méthodes utilitaires pour les exemples
    private void applyPublicIpSecurityRules(String ip) { }
    private void grantInternalAccess() { }
    private void enableDevelopmentFeatures() { }
    private void saveAuditLog(AuditLog log) { }
    private void incrementCounter(String name) { }
    private long getCounter(String name) { return 0; }
    private String lookupLocationIPv4(String ip) { return "Unknown"; }
    private String lookupLocationIPv6(String ip) { return "Unknown"; }
    
    // Classes DTO
    record IpInfoDTO(
        String address,
        String version,
        boolean isPublic,
        boolean isPrivate,
        String networkType
    ) {}
    
    static class AuditLog {
        private String userId;
        private String action;
        private String ipAddress;
        private String ipVersion;
        private String networkType;
        private java.time.LocalDateTime timestamp;
        
        // Getters et setters
        public void setUserId(String userId) { this.userId = userId; }
        public void setAction(String action) { this.action = action; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public void setIpVersion(String ipVersion) { this.ipVersion = ipVersion; }
        public void setNetworkType(String networkType) { this.networkType = networkType; }
        public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}