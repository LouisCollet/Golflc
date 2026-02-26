
package info_test;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import static interfaces.Log.LOG;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Bean ApplicationScoped gérant l'accès à la base de données GeoIP2.
 * Configure et maintient une instance unique de DatabaseReader pour
 * toute l'application avec mise en cache optimale.
 */
@ApplicationScoped
public class GeoIpDatabase {
    
    private static final String DEFAULT_DB_PATH = "/opt/geo/GeoLite2-City.mmdb";
    private static final int DEFAULT_CACHE_SIZE = 4096;
    
    private DatabaseReader reader;
    private volatile boolean initialized = false;
    
    /**
     * Chemin vers la base de données GeoIP2
     */
    @Inject
    @ConfigProperty(name = "geoip.database.path", defaultValue = DEFAULT_DB_PATH)
    String databasePath;
    
    /**
     * Taille du cache en nombre d'entrées
     */
    @Inject
    @ConfigProperty(name = "geoip.cache.size", defaultValue = "4096")
    int cacheSize;
    
    /**
     * Mode de lecture du fichier: MEMORY (charge en RAM), MEMORY_MAPPED (mapping mémoire)
     */
    @Inject
    @ConfigProperty(name = "geoip.file.mode", defaultValue = "MEMORY_MAPPED")
    String fileMode;
    
    /**
     * Initialise le DatabaseReader au démarrage de l'application
     */
    @PostConstruct
    void init() {
        LOG.info("Initializing GeoIP database from path: {}", databasePath);
        
        try {
            validateDatabaseFile(databasePath);
            
            File dbFile = new File(databasePath);
            Reader.FileMode mode = parseFileMode(fileMode);
            
            reader = new DatabaseReader.Builder(dbFile)
                    .fileMode(mode)
                    .withCache(new CHMCache(cacheSize))
                    .build();
            
            initialized = true;
            
            LOG.info("GeoIP database initialized successfully - Mode: {}, Cache size: {}", 
                     mode, cacheSize);
            logDatabaseInfo();
            
        } catch (IOException e) {
            LOG.error("Failed to initialize GeoIP database from path: {}", databasePath, e);
            reader = null;
            initialized = false;
            throw new IllegalStateException(
                "Cannot initialize GeoIP database. Please check the file path and permissions: " + 
                databasePath, e);
        } catch (Exception e) {
            LOG.error("Unexpected error initializing GeoIP database", e);
            reader = null;
            initialized = false;
            throw new IllegalStateException("GeoIP database initialization failed", e);
        }
    }
    
    /**
     * Ferme proprement le DatabaseReader lors de la destruction du bean
     */
    @PreDestroy
    void cleanup() {
        if (reader != null) {
            try {
                LOG.info("Closing GeoIP database reader");
                reader.close();
                initialized = false;
                LOG.debug("GeoIP database reader closed successfully");
            } catch (IOException e) {
                LOG.warn("Error closing GeoIP database reader", e);
            }
        }
    }
    
    /**
     * Obtient le DatabaseReader
     * 
     * @return Le DatabaseReader ou null si non initialisé
     */
    public DatabaseReader getReader() {
        return reader;
    }
    
    /**
     * Vérifie si la base de données est initialisée et prête
     * 
     * @return true si le reader est disponible
     */
    public boolean isAvailable() {
        return initialized && reader != null;
    }
    
    /**
     * Obtient le chemin de la base de données configurée
     * 
     * @return Le chemin du fichier
     */
    public String getDatabasePath() {
        return databasePath;
    }
    
    /**
     * Obtient les métadonnées de la base de données
     * 
     * @return Optional contenant les métadonnées si disponibles
     */
    public Optional<DatabaseMetadata> getMetadata() {
        if (!isAvailable()) {
            return Optional.empty();
        }
        
        try {
            var dbMetadata = reader.metadata();
            
          return Optional.of(new DatabaseMetadata(
                dbMetadata.databaseType(),
             //   dbMetadata.buildEpoch().longValueExact(),
              //      dbMetadata.buildTime();
                LocalDateTime.ofInstant(dbMetadata.buildTime(), ZoneOffset.systemDefault()),
                dbMetadata.description().getOrDefault("en", "Unknown")
            ));
        } catch (Exception e) {
            LOG.warn("Error reading database metadata", e);
            return Optional.empty();
        }
    }
    
    /**
     * Valide que le fichier de base de données existe et est lisible
     */
    private void validateDatabaseFile(String path) throws IOException {
        Path dbPath = Paths.get(path);
        
        if (!Files.exists(dbPath)) {
            throw new IOException("GeoIP database file not found: " + path);
        }
        
        if (!Files.isRegularFile(dbPath)) {
            throw new IOException("GeoIP database path is not a regular file: " + path);
        }
        
        if (!Files.isReadable(dbPath)) {
            throw new IOException("GeoIP database file is not readable: " + path);
        }
        
        long fileSize = Files.size(dbPath);
        if (fileSize == 0) {
            throw new IOException("GeoIP database file is empty: " + path);
        }
        
        LOG.debug("Database file validated - Size: {} bytes ({})", 
                 fileSize, formatFileSize(fileSize));
    }
    
    /**
     * Parse le mode de fichier à partir de la configuration
     */
    private Reader.FileMode parseFileMode(String mode) {
        try {
            return Reader.FileMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid file mode '{}', using MEMORY_MAPPED as default", mode);
            return Reader.FileMode.MEMORY_MAPPED;
        }
    }
    
    /**
     * Log les informations sur la base de données chargée
     */
    private void logDatabaseInfo() {
        getMetadata().ifPresent(metadata -> {
            LOG.info("GeoIP Database Type: {}", metadata.databaseType());
            LOG.info("GeoIP Build Date: {}", metadata.buildDateTime);
            LOG.info("GeoIP Description: {}", metadata.description());
        });
    }
    
    /**
     * Formate la taille de fichier de manière lisible
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), unit);
    }
    
    /**
     * Record contenant les métadonnées de la base de données
     */
    public record DatabaseMetadata(
        String databaseType,
      //  long buildEpoch,  mod 11/01/2026
        LocalDateTime buildDateTime,
        String description
    ) {}
}
