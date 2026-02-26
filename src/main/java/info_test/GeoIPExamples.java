package info_test;

import info_test.GeoDetector;
//import info_test.IpDetector;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Exemples d'utilisation du GeoIP simple
 */
@ApplicationScoped
@Named("geoIPExamples")
public class GeoIPExamples {
    @Inject
    HttpServletRequest request;
    // ========================================================================
    // EXEMPLE 1: Utilisation la plus simple
    // ========================================================================
    
    public void example1_Simple() {
        // Obtenir l'IP
        String ip = IpDetector.getClientIp(request);
        
        // Obtenir la localisation
        String location = GeoDetector.getLocation(ip);
        
        LOG.debug("Vous êtes à: " + location);
        // Résultat: "Vous êtes à: Paris, France"
    }
    
    // ========================================================================
    // EXEMPLE 2: Informations séparées
    // ========================================================================
    
    public void example2_Separate(HttpServletRequest request) {
        String ip = IpDetector.getClientIp(request);
        
        String city = GeoDetector.getCity(ip);
        String country = GeoDetector.getCountry(ip);
        String countryCode = GeoDetector.getCountryCode(ip);
        
        LOG.info("City: {}, Country: {} ({})", city, country, countryCode);
        // Résultat: "City: Paris, Country: France (FR)"
    }
    
    // ========================================================================
    // EXEMPLE 3: Coordonnées GPS
    // ========================================================================
    
    public void example3_Coordinates(HttpServletRequest request) {
        String ip = IpDetector.getClientIp(request);
        
        GeoDetector.Coordinates coords = GeoDetector.getCoordinates(ip);
        
        if (coords != null) {
            System.out.println("GPS: " + coords);
            System.out.println("Latitude: " + coords.latitude());
            System.out.println("Longitude: " + coords.longitude());
        }
        // Résultat: "GPS: [48.8566, 2.3522]"
    }
    
    // ========================================================================
    // EXEMPLE 4: Informations complètes
    // ========================================================================
    
    public void example4_FullInfo(HttpServletRequest request) {
        String ip = IpDetector.getClientIp(request);
        
        GeoDetector.GeoInfo info = GeoDetector.getFullInfo(ip);
        
        LOG.info("IP: {}", info.ip());
        LOG.info("Location: {}", info.getLocation());
        LOG.info("City: {}", info.city());
        LOG.info("Country: {}", info.country());
        LOG.info("Country Code: {}", info.countryCode());
        LOG.info("Type: {}", info.type());
        
        if (info.hasCoordinates()) {
            LOG.info("Coordinates: {}", info.coordinates());
        }
        
        // Ou simplement
        LOG.info("{}", info);
    }
    
    // ========================================================================
    // EXEMPLE 5: REST API
    // ========================================================================
    
    @Path("/api/location")
    public static class LocationResource {
        
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getLocation(@Context HttpServletRequest request) {
            String ip = IpDetector.getClientIp(request);
            return GeoDetector.getLocation(ip);
        }
        
        @GET
        @Path("/country")
        @Produces(MediaType.TEXT_PLAIN)
        public String getCountry(@Context HttpServletRequest request) {
            String ip = IpDetector.getClientIp(request);
            return GeoDetector.getCountry(ip);
        }
        
        @GET
        @Path("/full")
        @Produces(MediaType.APPLICATION_JSON)
        public GeoDetector.GeoInfo getFullInfo(@Context HttpServletRequest request) {
            String ip = IpDetector.getClientIp(request);
            return GeoDetector.getFullInfo(ip);
        }
    }
    
    // ========================================================================
    // EXEMPLE 6: Restriction géographique
    // ========================================================================
    
    public boolean checkGeographicAccess(HttpServletRequest request) {
        String ip = IpDetector.getClientIp(request);
        String countryCode = GeoDetector.getCountryCode(ip);
        
        // Autoriser uniquement France, Belgique, Suisse
        if (countryCode != null && 
            (countryCode.equals("FR") || 
             countryCode.equals("BE") || 
             countryCode.equals("CH"))) {
            return true;
        }
        
        LOG.warn("Access denied for country: {}", countryCode);
        return false;
    }
    
    // ========================================================================
    // EXEMPLE 7: Logging avec localisation
    // ========================================================================
    
    public void logUserAction(HttpServletRequest request, String userId, String action) {
        String ip = IpDetector.getClientIp(request);
        String location = GeoDetector.getLocation(ip);
        
        LOG.info("User {} performed {} from {} ({})", 
                 userId, action, ip, location);
        // Résultat: "User john performed login from 203.0.113.1 (Paris, France)"
    }
    
    // ========================================================================
    // EXEMPLE 8: Service CDI
    // ========================================================================
    
    @jakarta.enterprise.context.ApplicationScoped
    public static class UserLocationService {
        
        @Inject
        HttpServletRequest request;
        
        public String getUserLocation() {
            String ip = IpDetector.getClientIp(request);
            return GeoDetector.getLocation(ip);
        }
        
        public boolean isUserInEurope() {
            String ip = IpDetector.getClientIp(request);
            String countryCode = GeoDetector.getCountryCode(ip);
            
            // Liste simplifiée des codes pays européens
            return countryCode != null && (
                countryCode.equals("FR") || countryCode.equals("BE") || 
                countryCode.equals("DE") || countryCode.equals("IT") || 
                countryCode.equals("ES") || countryCode.equals("PT") ||
                countryCode.equals("NL") || countryCode.equals("CH")
            );
        }
        
        public GeoDetector.GeoInfo getUserGeoInfo() {
            String ip = IpDetector.getClientIp(request);
            return GeoDetector.getFullInfo(ip);
        }
    }
    
    // ========================================================================
    // EXEMPLE 9: Combiné IP + GeoIP
    // ========================================================================
    
    public void example9_Combined(HttpServletRequest request) {
        // Obtenir l'IP
        String ip = IpDetector.getClientIp(request);
        
        // Info IP
        String ipDesc = IpDetector.getIpDescription(ip);
        LOG.info("IP Info: {}", ipDesc);
        
        // Info GeoIP
        String location = GeoDetector.getLocation(ip);
        LOG.info("Location: {}", location);
        
        // Combiné
        LOG.info("User connected from {} - Location: {}", ipDesc, location);
        // Résultat: "User connected from IPv4 publique: 203.0.113.1 - Location: Paris, France"
    }
    
    // ========================================================================
    // EXEMPLE 10: Statistiques
    // ========================================================================
    
    @jakarta.enterprise.context.ApplicationScoped
    public static class GeoStatisticsService {
        
        public void recordVisit(HttpServletRequest request, String pageUrl) {
            String ip = IpDetector.getClientIp(request);
            GeoDetector.GeoInfo geoInfo = GeoDetector.getFullInfo(ip);
            
            // Créer un enregistrement de visite
            PageVisit visit = new PageVisit();
            visit.pageUrl = pageUrl;
            visit.ip = ip;
            visit.ipVersion = IpDetector.getIpVersion(ip);
            visit.city = geoInfo.city();
            visit.country = geoInfo.country();
            visit.countryCode = geoInfo.countryCode();
            
            if (geoInfo.hasCoordinates()) {
                visit.latitude = geoInfo.coordinates().latitude();
                visit.longitude = geoInfo.coordinates().longitude();
            }
            
            // Sauvegarder en base de données
            // em.persist(visit);
            
            LOG.info("Visit recorded: {}", visit);
        }
        
        static class PageVisit {
            String pageUrl;
            String ip;
            int ipVersion;
            String city;
            String country;
            String countryCode;
            Double latitude;
            Double longitude;
            
            @Override
            public String toString() {
                return String.format("PageVisit{url='%s', ip='%s' (IPv%d), location='%s, %s' (%s)}",
                    pageUrl, ip, ipVersion, city, country, countryCode);
            }
        }
    }
}