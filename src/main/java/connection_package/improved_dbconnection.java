package connection_package;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import utils.LCUtil;

/* suggestion Claude Code
 * Classe utilitaire pour la gestion des connexions JDBC à MySQL.
 * <p>
 * Cette classe charge la configuration depuis jdbc.properties et
 * récupère les credentials depuis les variables d'environnement.
 * </p>
 */
public final class improved_dbconnection {
    
 //   private static final String CLASSNAME = LCUtil.getCurrentClassName();
    private static final String PROPERTIES_FILE = "jdbc.properties";
    
    // Clés de propriétés
    private static final String PROP_JDBC_MYSQL = "jdbc.mysql";
    private static final String PROP_JDBC_HOST = "jdbc.host";
    private static final String PROP_JDBC_DBNAME = "jdbc.dbname";
    private static final String PROP_JDBC_PARAMS = "jdbc.params";
    
    // Variables d'environnement
    private static final String ENV_USERNAME = "MYSQL_USERNAME";
    private static final String ENV_PASSWORD = "MYSQL_PASSWORD";
    
    private improved_dbconnection() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    
    /**
     * Retourne une nouvelle connexion JDBC à la base de données MySQL.
     * <p>
     * La connexion est créée à partir des propriétés définies dans jdbc.properties
     * et des credentials stockés dans les variables d'environnement.
     * </p>
     * <p>
     * <strong>Important:</strong> L'appelant est responsable de la fermeture de la connexion.
     * Utilisez try-with-resources pour garantir la fermeture.
     * </p>
     *
     * @return une connexion JDBC active
     * @throws SQLException si une erreur SQL survient lors de la connexion
     * @throws IllegalStateException si les variables d'environnement sont manquantes
     * @throws IOException si le fichier de propriétés ne peut être chargé
     */
    public static Connection getConnection() throws SQLException, IOException {
        final String methodName = LCUtil.getCurrentMethodName();
        String jdbcUrl = null;
        try {
            LOG.debug("Opening JDBC connection");
            
            // Charger les propriétés
            Properties props = loadProperties();
            
            // Construire l'URL JDBC
            jdbcUrl = buildJdbcUrl(props);
            LOG.debug("JDBC URL = {}", jdbcUrl);
            
            // Récupérer les credentials
            Credentials credentials = getCredentials();
            
            // Créer la connexion
            Connection conn = DriverManager.getConnection(
                jdbcUrl, 
                credentials.username, 
                credentials.password
            );
            
            logMetaData(conn);
            LOG.debug("JDBC connection successfully opened");
            
            return conn;
            
        } catch (SQLException e) {
            LOG.error("SQLException while opening connection to {}", jdbcUrl, e);
            handleSQLException(e, methodName);
            throw e; // Rethrow après logging
            
        } catch (IllegalStateException | IOException e) {
            LOG.error("Error while opening database connection", e);
            handleGenericException(e, methodName);
            throw e; // Rethrow après logging
        }
    }
    
    /**
     * Construit l'URL JDBC à partir des propriétés.
     *
     * @param props propriétés chargées depuis jdbc.properties
     * @return l'URL JDBC complète
     * @throws IllegalStateException si une propriété requise est manquante
     */
    private static String buildJdbcUrl(Properties props) {
        String mysql = getRequiredProperty(props, PROP_JDBC_MYSQL);
        String host = getRequiredProperty(props, PROP_JDBC_HOST);
        String dbname = getRequiredProperty(props, PROP_JDBC_DBNAME);
        String params = props.getProperty(PROP_JDBC_PARAMS, ""); // Optionnel
        
        return mysql + host + dbname + params;
    }
    
    /**
     * Récupère une propriété requise ou lance une exception si absente.
     *
     * @param props les propriétés
     * @param key la clé de la propriété
     * @return la valeur de la propriété
     * @throws IllegalStateException si la propriété est absente ou vide
     */
    private static String getRequiredProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                "Missing or empty required property: " + key
            );
        }
        return value;
    }
    
    /**
     * Récupère les credentials depuis les variables d'environnement.
     *
     * @return les credentials
     * @throws IllegalStateException si les variables d'environnement sont manquantes
     */
    private static Credentials getCredentials() {
        String username = System.getenv(ENV_USERNAME);
        String password = System.getenv(ENV_PASSWORD);
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException(
                "Missing environment variable: " + ENV_USERNAME
            );
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalStateException(
                "Missing environment variable: " + ENV_PASSWORD
            );
        }
        
        return new Credentials(username, password);
    }
    
    /**
     * Log les métadonnées de la connexion JDBC.
     *
     * @param conn la connexion JDBC
     */
    private static void logMetaData(Connection conn) {
        if (conn == null) {
            return;
        }
        
        try {
            DatabaseMetaData meta = conn.getMetaData();
            LOG.debug("JDBC version        = {}.{}", 
                meta.getJDBCMajorVersion(), 
                meta.getJDBCMinorVersion()
            );
            LOG.debug("JDBC driver version = {}", meta.getDriverVersion());
            LOG.debug("Connected database  = {}", conn.getCatalog());
        } catch (SQLException e) {
            LOG.warn("Unable to retrieve database metadata", e);
        }
    }
    
    /**
     * Charge le fichier jdbc.properties depuis le classpath.
     * <p>
     * Affiche également les propriétés chargées en mode debug.
     * </p>
     *
     * @return les propriétés chargées
     * @throws IOException si le fichier ne peut être chargé
     */
    private static Properties loadProperties() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        try (InputStream inputStream = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new IOException(
                    "Resource not found on classpath: " + PROPERTIES_FILE
                );
            }
            
            Properties props = new Properties();
            props.load(inputStream);
            
            // Log des propriétés en mode debug
            LCUtil.printProperties(PROPERTIES_FILE);
            
            return props;
            
        } catch (IOException e) {
            LOG.error("Unable to load {}", PROPERTIES_FILE, e);
            throw e;
        }
    }
    
    /**
     * Classe interne pour encapsuler les credentials.
     */
    private static class Credentials {
        final String username;
        final String password;
        
        Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
