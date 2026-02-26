
package connection_package;

import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Producer CDI pour la connexion MySQL via le datasource WildFly
 * jndi-name="java:jboss/datasources/golflc" dans mysql-ds.xml
 */
@RequestScoped  // pas application ?
public class ConnectionProducer {
    /**
     * Injection du DataSource JNDI défini dans mysql-ds.xml
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;
    @Produces
    @RequestScoped
    public Connection produceConnection() throws SQLException {
        LOG.debug("connexion produced !");
        return dataSource.getConnection();
    }
    /**
     * Ferme la connection automatiquement en fin de requête
     */
    public void closeConnection(@Disposes Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close(); // retourne au pool
            }
        } catch (SQLException ignored) {}
    }
}