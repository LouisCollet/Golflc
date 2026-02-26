package Controllers;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.primefaces.config.PrimeEnvironment;

@Named("infoC3")
@ApplicationScoped
public class InfoController3 implements Serializable, interfaces.GolfInterface, interfaces.Log {
    
    // ===== VARIABLES STATIQUES (ancien code) =====
    private static Attributes manifestAttributes = null;
    private static String JQueryVersion;
    
    // ===== VARIABLES D'INSTANCE (nouveau code) =====
    private String groupId;
    private String artifactId;
    private String version;
    private String buildTime;
    private String applicationName;
    
    // ===== CONSTRUCTEUR =====
    /**
     * Constructeur - charge le MANIFEST initial
     */
    public InfoController3() throws IOException {
        LOG.debug("entering InfoController constructor");
        
        try (InputStream inputStream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (inputStream != null) {
                manifestAttributes = new Manifest(inputStream).getMainAttributes();
                LOG.debug("MANIFEST.MF loaded successfully");
            } else {
                LOG.debug("infoController : META-INF/MANIFEST.MF is null");
            }
        } catch (Exception e) {
            LOG.debug("exception in InfoController constructor: " + e);
        }
    }
    
    // ===== POST CONSTRUCT =====
    /**
     * PostConstruct - charge les propriétés Maven et détails du MANIFEST
     */
    @PostConstruct
    public void init() {
        LOG.debug("Entering InfoController @PostConstruct init()");
        loadMavenProperties();
        loadManifestInfo();
    }
    
    // ===== MÉTHODES DE CHARGEMENT (nouveau code) =====
    /**
     * Charge les propriétés depuis pom.properties (généré automatiquement par Maven)
     */
    private void loadMavenProperties() {
        Properties props = new Properties();
        String pomPropertiesPath = "/META-INF/maven/lc/GolfWfly/pom.properties";
        
        try (InputStream is = getClass().getResourceAsStream(pomPropertiesPath)) {
            if (is != null) {
                props.load(is);
                this.groupId = props.getProperty("groupId");
                this.artifactId = props.getProperty("artifactId");
                this.version = props.getProperty("version");
                
                LOG.debug("Maven properties loaded: " + artifactId + " v" + version);
            } else {
                LOG.debug("pom.properties not found at: " + pomPropertiesPath);
                // Valeurs par défaut pour le développement
                this.groupId = "lc";
                this.artifactId = "GolfWfly";
                this.version = "DEV";
            }
        } catch (IOException e) {
            LOG.debug("Error loading pom.properties: " + e);
            // Valeurs par défaut en cas d'erreur
            this.groupId = "lc";
            this.artifactId = "GolfWfly";
            this.version = "ERROR";
        }
    }
    
    /**
     * Charge les informations depuis le MANIFEST.MF
     */
    private void loadManifestInfo() {
        if (manifestAttributes != null) {
            // Lecture des entrées custom du MANIFEST
            this.buildTime = manifestAttributes.getValue("Build-Time");
            this.applicationName = manifestAttributes.getValue("Application-Name");
            
            // Alternative: utiliser les entrées par défaut si version pas encore chargée
            if (this.version == null || "DEV".equals(this.version) || "ERROR".equals(this.version)) {
                String implVersion = manifestAttributes.getValue("Implementation-Version");
                if (implVersion != null) {
                    this.version = implVersion;
                }
            }
            
            LOG.debug("Manifest info loaded. Build time: " + buildTime);
        } else {
            LOG.debug("manifestAttributes is null, using default values");
            this.buildTime = "N/A";
            this.applicationName = "GolfLC WHS v3.0";
        }
    }
    
    // ===== VOS ANCIENNES MÉTHODES =====
    /**
     * Méthode login appelée depuis login.xhtml
     */
    public void login() {
        LOG.debug("entering login() coming from login.xhtml");
    }
    
    // ===== GETTERS POUR LES NOUVELLES PROPRIÉTÉS =====
    public String getGroupId() {
        return groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getBuildTime() {
        return buildTime;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    /**
     * Retourne les informations complètes de l'application
     */
    public String getFullInfo() {
        return String.format("%s - %s:%s:%s (Build: %s)", 
            applicationName != null ? applicationName : "N/A", 
            groupId != null ? groupId : "N/A", 
            artifactId != null ? artifactId : "N/A", 
            version != null ? version : "N/A", 
            buildTime != null ? buildTime : "N/A");
    }
    
    // ===== GETTERS/SETTERS POUR L'ANCIEN CODE =====
    public static Attributes getManifestAttributes() {
        return manifestAttributes;
    }
    
    public static String getJQueryVersion() {
        return JQueryVersion;
    }
    
    public static void setJQueryVersion(String jQueryVersion) {
        JQueryVersion = jQueryVersion;
    }
    
    // ===== MÉTHODES UTILITAIRES (si vous en aviez d'autres, ajoutez-les ici) =====
    
    /**
     * Récupère une valeur spécifique du MANIFEST
     * @param key La clé à rechercher
     * @return La valeur ou null si non trouvée
     */
    public String getManifestValue(String key) {
        if (manifestAttributes != null) {
            return manifestAttributes.getValue(key);
        }
        return null;
    }
    
    /**
     * Affiche toutes les informations du MANIFEST (utile pour debug)
     */
    public void logAllManifestEntries() {
        if (manifestAttributes != null) {
            LOG.debug("=== MANIFEST Entries ===");
            manifestAttributes.forEach((key, value) -> 
                LOG.debug(key + " = " + value)
            );
        } else {
            LOG.debug("No MANIFEST attributes available");
        }
    }
    
    
    
    
    
}