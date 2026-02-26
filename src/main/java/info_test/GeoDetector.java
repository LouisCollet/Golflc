package info_test;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import static interfaces.Log.LOG;

/**
 * Service GeoIP simple - fonctionne partout
 * Usage: GeoIP.getLocation(ip) ou injection CDI
 */
@ApplicationScoped
public class GeoDetector {
    
    private static final String DEFAULT_DB_PATH = "/opt/geo/GeoLite2-City.mmdb";
    private static DatabaseReader reader;
    
    @PostConstruct
    void init() {
        initDatabase(DEFAULT_DB_PATH);
    }
    
    @PreDestroy
    void cleanup() {
        closeDatabase();
    }
    
    /**
     * Initialise la base de données GeoIP
     * Peut être appelé manuellement si besoin
     */
    public static void initDatabase(String dbPath) {
        try {
            File database = new File(dbPath);
            
            if (!database.exists()) {
                LOG.warn("GeoIP database not found at: {}", dbPath);
                return;
            }
            
            reader = new DatabaseReader.Builder(database).build();
            LOG.info("GeoIP database loaded successfully from: {}", dbPath);
            
        } catch (IOException e) {
            LOG.error("Failed to load GeoIP database from: {}", dbPath, e);
            reader = null;
        }
    }
    
    /**
     * Ferme la base de données
     */
    public static void closeDatabase() {
        if (reader != null) {
            try {
                reader.close();
                LOG.info("GeoIP database closed");
            } catch (IOException e) {
                LOG.warn("Error closing GeoIP database", e);
            }
        }
    }
    
    /**
     * Vérifie si la base de données est disponible
     */
    public static boolean isAvailable() {
        return reader != null;
    }
    
    /**
     * Obtient la localisation d'une IP (format simple: "Ville, Pays")
     */
    public static String getLocation(String ip) {
        LOG.debug("entering getLocation with ip = " + ip);
        if (!isAvailable()) {
            return "GeoIP non disponible";
        }
        
        if (ip == null || ip.isBlank() || "unknown".equals(ip)) {
            return "IP inconnue : null, blank, etc";
        }
        
        // Ne traiter que les IP publiques
        if (IpDetector.isPrivateIp(ip)) {
            return "IP privée";
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            
            String city = getCityName(response);
            String country = getCountryName(response);
            
            return city + ", " + country;
            
        } catch (Exception e) {
            LOG.debug("Cannot get location for IP: {}", ip, e);
            return "Localisation inconnue";
        }
    }
    
    /**
     * Obtient uniquement le pays
     */
    public static String getCountry(String ip) {
        if (!isAvailable() || ip == null || IpDetector.isPrivateIp(ip)) {
            return null;
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            return getCountryName(response);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtient uniquement la ville
     */
    public static String getCity(String ip) {
        if (!isAvailable() || ip == null || IpDetector.isPrivateIp(ip)) {
            return null;
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            return getCityName(response);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtient le code pays (ISO)
     */
    public static String getCountryCode(String ip) {
        if (!isAvailable() || ip == null || IpDetector.isPrivateIp(ip)) {
            return null;
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            Country country = response.country();
            return country != null ? country.isoCode() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtient les coordonnées GPS
     */
    public static Coordinates getCoordinates(String ip) {
        if (!isAvailable() || ip == null || IpDetector.isPrivateIp(ip)) {
            return null;
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            Location location = response.location();
            
            if (location != null && location.latitude() != null && location.longitude() != null) {
                return new Coordinates(location.latitude(), location.longitude());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtient toutes les informations détaillées
     */
    public static GeoInfo getFullInfo(String ip) {
        if (!isAvailable()) {
            return new GeoInfo(ip, null, null, null, null, null);
        }
        
        if (ip == null || ip.isBlank() || "unknown".equals(ip)) {
            return new GeoInfo(ip, null, null, null, null, null);
        }
        
        if (IpDetector.isPrivateIp(ip)) {
            return new GeoInfo(ip, null, null, null, null, "private");
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            
            String city = getCityName(response);
            String country = getCountryName(response);
            String countryCode = response.country() != null ? response.country().isoCode() : null;
            
            Coordinates coords = null;
            Location location = response.location();
            if (location != null && location.latitude() != null && location.longitude() != null) {
                coords = new Coordinates(location.latitude(), location.longitude());
            }
            
            return new GeoInfo(ip, city, country, countryCode, coords, "public");
            
        } catch (Exception e) {
            LOG.debug("Cannot get full info for IP: {}", ip, e);
            return new GeoInfo(ip, null, null, null, null, "unknown");
        }
    }
    
    /**
     * Extrait le nom de la ville (en français si disponible)
     */
    private static String getCityName(CityResponse response) {
        City city = response.city();
        if (city == null) {
            return "Ville inconnue";
        }
        
        // Essayer français, sinon anglais, sinon premier disponible
        String name = city.names().get("fr");
        if (name == null) {
            name = city.name();
        }
        
        return name != null ? name : "Ville inconnue";
    }
    
    /**
     * Extrait le nom du pays (en français si disponible)
     */
    private static String getCountryName(CityResponse response) {
        Country country = response.country();
        if (country == null) {
            return "Pays inconnu";
        }
        
        // Essayer français, sinon anglais, sinon premier disponible
        String name = country.names().get("fr");
        if (name == null) {
            name = country.name();
        }
        
        return name != null ? name : "Pays inconnu";
    }
    
    /**
     * Record pour les coordonnées GPS
     */
    public record Coordinates(double latitude, double longitude) {
        @Override
        public String toString() {
            return String.format("[%.4f, %.4f]", latitude, longitude);
        }
    }
    
    /**
     * Record pour les informations géographiques complètes
     */
    public record GeoInfo(
        String ip,
        String city,
        String country,
        String countryCode,
        Coordinates coordinates,
        String type
    ) {
        public String getLocation() {
            if (city != null && country != null) {
                return city + ", " + country;
            }
            if (country != null) {
                return country;
            }
            if ("private".equals(type)) {
                return "IP privée";
            }
            return "Localisation inconnue";
        }
        
        public boolean hasCoordinates() {
            return coordinates != null;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("GeoInfo{ip=").append(ip);
            if (city != null) sb.append(", city=").append(city);
            if (country != null) sb.append(", country=").append(country);
            if (countryCode != null) sb.append(" (").append(countryCode).append(")");
            if (coordinates != null) sb.append(", coords=").append(coordinates);
            sb.append(", type=").append(type).append("}");
            return sb.toString();
        }
    }
}
