package info_test;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import static interfaces.Log.LOG;

/**
 * Provider d'informations de géolocalisation basé sur GeoIP2.
 * Utilise la base de données MaxMind GeoLite2 pour résoudre
 * la localisation d'une adresse IP.
 */
@ApplicationScoped
public class GeoIpInfoProvider implements InfoProvider {
    
    private static final String UNKNOWN_CITY = "Ville inconnue";
    private static final String UNKNOWN_COUNTRY = "Pays inconnu";
    private static final String PRIVATE_IP_MESSAGE = "IP privée ou inconnue";
    private static final String GEOIP_UNAVAILABLE = "GeoIP indisponible";
    
    @Inject
    GeoIpDatabase geoDb;
    
    @Inject
    NetworkInfoProvider networkInfoProvider;
    
    /**
     * Langue par défaut pour les noms de localisation.
     * Exemples: fr, en, es, de, it, pt, ru, zh-CN, ja
     */
    @Inject
    @ConfigProperty(name = "geoip.locale.language", defaultValue = "fr")
    String localeLanguage;
    
    /**
     * Pays par défaut pour la locale (optionnel).
     * Exemples: FR, US, ES, DE
     */
    @Inject
    @ConfigProperty(name = "geoip.locale.country")
    Optional<String> localeCountry;
    
    /**
     * Format d'affichage de la localisation.
     * Valeurs possibles: simple, detailed, coordinates
     */
    @Inject
    @ConfigProperty(name = "geoip.display.format", defaultValue = "simple")
    String displayFormat;
    
    /**
     * Inclure le code pays dans l'affichage
     */
    @Inject
    @ConfigProperty(name = "geoip.display.include-country-code", defaultValue = "false")
    boolean includeCountryCode;
    
    @Override
    public String name() {
        return "GeoIP";
    }
    
    @Override
    public String get() {
        return getWithConfiguredLocale();
    }
    
    /**
     * Obtient la localisation avec la locale configurée
     */
    public String getWithConfiguredLocale() {
        Locale locale = buildConfiguredLocale();
        LOG.debug("Using configured locale: {}", locale);
        return getWithLocale(locale);
    }
    
    /**
     * Obtient la localisation avec une locale spécifique
     * 
     * @param locale La locale à utiliser pour les noms
     * @return La localisation formatée ou un message d'erreur
     */
    public String getWithLocale(Locale locale) {
        try {
            InetAddress ip = resolveClientIp();
            if (ip == null) {
                LOG.warn("Unable to resolve client IP");
                return GEOIP_UNAVAILABLE;
            }
            
            LOG.debug("Resolving GeoIP for address: {}", ip.getHostAddress());
            
            CityResponse response = geoDb.getReader().city(ip);
            
            return formatResponse(response, locale);
            
        } catch (AddressNotFoundException e) {
            LOG.debug("IP address not found in GeoIP database: {}", e.getMessage());
            return PRIVATE_IP_MESSAGE;
        } catch (GeoIp2Exception | IOException e) {
            LOG.error("GeoIP error: {}", e.getMessage(), e);
            return GEOIP_UNAVAILABLE;
        } catch (Exception e) {
            LOG.error("Unexpected error in GeoIP resolution: {}", e.getMessage(), e);
            return GEOIP_UNAVAILABLE;
        }
    }
    
    /**
     * Obtient des informations détaillées sur la localisation
     * 
     * @return Un objet GeoLocationInfo avec toutes les informations disponibles
     */
    public GeoLocationInfo getDetailedInfo() {
        try {
            InetAddress ip = resolveClientIp();
            if (ip == null) {
                return null;
            }
            
            CityResponse response = geoDb.getReader().city(ip);
            Locale locale = buildConfiguredLocale();
            
            return new GeoLocationInfo(response, locale);
            
        } catch (Exception e) {
            LOG.error("Error getting detailed GeoIP info: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Construit la locale à partir de la configuration
     */
    private Locale buildConfiguredLocale() {
        Locale.Builder builder = new Locale.Builder()
            .setLanguage(localeLanguage);
        
        if (localeCountry.isPresent() && !localeCountry.get().isBlank()) {
            builder.setRegion(localeCountry.get());
        }
        
        return builder.build();
    }
    
    /**
     * Formate la réponse GeoIP selon le format configuré
     */
    private String formatResponse(CityResponse response, Locale locale) {
        String city = extractLocalizedName(response.city(), locale, UNKNOWN_CITY);
        String country = extractLocalizedName(response.country(), locale, UNKNOWN_COUNTRY);
        String countryCode = response.country() != null ? 
            response.country().isoCode() : null;
        
        switch (displayFormat.toLowerCase()) {
            case "detailed":
                return formatDetailed(response, city, country, countryCode, locale);
            case "coordinates":
                return formatWithCoordinates(city, country, response.location());
            case "simple":
            default:
                return formatSimple(city, country, countryCode);
        }
    }
    
    /**
     * Format simple: "Ville, Pays" ou "Ville, Pays (XX)"
     */
    private String formatSimple(String city, String country, String countryCode) {
        if (includeCountryCode && countryCode != null) {
            return String.format("%s, %s (%s)", city, country, countryCode);
        }
        return String.format("%s, %s", city, country);
    }
    
    /**
     * Format détaillé: "Ville, Région, Pays (XX) - Code postal"
     */
    private String formatDetailed(CityResponse response, String city, String country, 
                                  String countryCode, Locale locale) {
        StringBuilder result = new StringBuilder();
        result.append(city);
        
        // Ajouter la région/subdivision si disponible
        if (response.mostSpecificSubdivision() != null) {
            String subdivision = extractLocalizedName(
                response.mostSpecificSubdivision(), 
                locale, 
                null
            );
            if (subdivision != null) {
                result.append(", ").append(subdivision);
            }
        }
        
        result.append(", ").append(country);
        
        if (countryCode != null) {
            result.append(" (").append(countryCode).append(")");
        }
        
        // Ajouter le code postal si disponible
        Postal postal = response.postal();
        if (postal != null && postal.code() != null) {
            result.append(" - ").append(postal.code());
        }
        
        return result.toString();
    }
    
    /**
     * Format avec coordonnées: "Ville, Pays [lat, lon]"
     */
    private String formatWithCoordinates(String city, String country, Location location) {
        String base = formatSimple(city, country, null);
        
        if (location != null && location.latitude() != null && location.longitude() != null) {
            return String.format("%s [%.4f, %.4f]", 
                base, 
                location.latitude(), 
                location.longitude()
            );
        }
        
        return base;
    }
    
    /**
     * Extrait le nom localisé d'un City record
     */
    private String extractLocalizedName(City record, Locale locale, String defaultValue) {
        return extractNameFromMap(record != null ? record.names() : null, 
                                  record != null ? record.name() : null, 
                                  locale, 
                                  defaultValue);
    }
    
    /**
     * Extrait le nom localisé d'un Country record
     */
    private String extractLocalizedName(Country record, Locale locale, String defaultValue) {
        return extractNameFromMap(record != null ? record.names() : null, 
                                  record != null ? record.name() : null, 
                                  locale, 
                                  defaultValue);
    }
    
    /**
     * Extrait le nom localisé d'un Continent record
     */
    private String extractLocalizedName(Continent record, Locale locale, String defaultValue) {
        return extractNameFromMap(record != null ? record.names() : null, 
                                  record != null ? record.name() : null, 
                                  locale, 
                                  defaultValue);
    }
    
    /**
     * Extrait le nom localisé d'un Subdivision record
     */
    private String extractLocalizedName(Subdivision record, Locale locale, String defaultValue) {
        return extractNameFromMap(record != null ? record.names() : null, 
                                  record != null ? record.name() : null, 
                                  locale, 
                                  defaultValue);
    }
    
    /**
     * Logique commune d'extraction de nom localisé avec fallback en cascade
     */
    private String extractNameFromMap(Map<String, String> names, 
                                      String defaultName, 
                                      Locale locale, 
                                      String defaultValue) {
        if (names == null || names.isEmpty()) {
            return defaultValue;
        }
        
        // 1. Essaie la langue complète (ex: fr-FR)
        String name = names.get(locale.toString().replace('_', '-'));
        
        // 2. Essaie juste la langue (ex: fr)
        if (name == null || name.isBlank()) {
            name = names.get(locale.getLanguage());
        }
        
        // 3. Essaie le nom par défaut (généralement en anglais)
        if (name == null || name.isBlank()) {
            name = defaultName;
        }
        
        // 4. Essaie explicitement l'anglais
        if (name == null || name.isBlank()) {
            name = names.get("en");
        }
        
        // 5. Prend le premier nom disponible
        if (name == null || name.isBlank()) {
            name = names.values().stream()
                .filter(n -> n != null && !n.isBlank())
                .findFirst()
                .orElse(null);
        }
        
        return name != null && !name.isBlank() ? name : defaultValue;
    }
    
    /**
     * Résout l'adresse IP du client
     */
    private InetAddress resolveClientIp() {
        try {
            String ipString = networkInfoProvider.get();
            if (ipString == null || ipString.isBlank() || "unknown".equals(ipString)) {
                return null;
            }
            return InetAddress.getByName(ipString);
        } catch (UnknownHostException e) {
            LOG.warn("Invalid IP address format: {}", e.getMessage());
            return null;
        }
    }
}

/**
 * Classe contenant toutes les informations de géolocalisation disponibles
 */
class GeoLocationInfo {
    private final String city;
    private final String country;
    private final String countryCode;
    private final String continent;
    private final String subdivision;
    private final String subdivisionCode;
    private final Double latitude;
    private final Double longitude;
    private final String postalCode;
    private final Integer accuracyRadius;
    private final String timezone;
    
    public GeoLocationInfo(CityResponse response, Locale locale) {
        this.city = extractName(response.city(), locale);
        this.country = extractName(response.country(), locale);
        this.countryCode = response.country() != null ? 
            response.country().isoCode() : null;
        this.continent = response.continent() != null ? 
            extractName(response.continent(), locale) : null;
        
        if (response.mostSpecificSubdivision() != null) {
            this.subdivision = extractName(response.mostSpecificSubdivision(), locale);
            this.subdivisionCode = response.mostSpecificSubdivision().isoCode();
        } else {
            this.subdivision = null;
            this.subdivisionCode = null;
        }
        
        Location location = response.location();
        if (location != null) {
            this.latitude = location.latitude();
            this.longitude = location.longitude();
            this.accuracyRadius = location.accuracyRadius();
            this.timezone = location.timeZone();
        } else {
            this.latitude = null;
            this.longitude = null;
            this.accuracyRadius = null;
            this.timezone = null;
        }
        
        Postal postal = response.postal();
        this.postalCode = postal != null ? postal.code() : null;
    }
    
    private String extractName(City record, Locale locale) {
        if (record == null) return null;
        
        Map<String, String> names = record.names();
        if (names == null) return null;
        
        String name = names.get(locale.getLanguage());
        if (name == null) {
            name = record.name();
        }
        return name;
    }
    
    private String extractName(Country record, Locale locale) {
        if (record == null) return null;
        
        Map<String, String> names = record.names();
        if (names == null) return null;
        
        String name = names.get(locale.getLanguage());
        if (name == null) {
            name = record.name();
        }
        return name;
    }
    
    private String extractName(Continent record, Locale locale) {
        if (record == null) return null;
        
        Map<String, String> names = record.names();
        if (names == null) return null;
        
        String name = names.get(locale.getLanguage());
        if (name == null) {
            name = record.name();
        }
        return name;
    }
    
    private String extractName(Subdivision record, Locale locale) {
        if (record == null) return null;
        
        Map<String, String> names = record.names();
        if (names == null) return null;
        
        String name = names.get(locale.getLanguage());
        if (name == null) {
            name = record.name();
        }
        return name;
    }
    
    // Getters
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getCountryCode() { return countryCode; }
    public String getContinent() { return continent; }
    public String getSubdivision() { return subdivision; }
    public String getSubdivisionCode() { return subdivisionCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPostalCode() { return postalCode; }
    public Integer getAccuracyRadius() { return accuracyRadius; }
    public String getTimezone() { return timezone; }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GeoLocationInfo{");
        sb.append("city='").append(city).append('\'');
        if (subdivision != null) {
            sb.append(", subdivision='").append(subdivision).append('\'');
        }
        sb.append(", country='").append(country).append('\'');
        if (countryCode != null) {
            sb.append(" (").append(countryCode).append(')');
        }
        if (hasCoordinates()) {
            sb.append(", coordinates=[").append(latitude).append(", ").append(longitude).append(']');
        }
        if (postalCode != null) {
            sb.append(", postal='").append(postalCode).append('\'');
        }
        if (timezone != null) {
            sb.append(", timezone='").append(timezone).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}