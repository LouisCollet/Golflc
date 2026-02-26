/*package connection_package;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import java.sql.Connection;

@ApplicationScoped
@ProdDB
public class ConnectionProducer implements ConnectionProvider {

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Override
    public Connection getConnection() throws Exception {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource non injecté !");
        }
        Connection conn = dataSource.getConnection();
            LOG.debug("PROD JDBC OPEN {}", System.identityHashCode(conn));
        return conn;
    }
}
*/
package connection_package;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
@ProdDB
public class ConnectionProducer implements ConnectionProvider {

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        DataSource ds = ensureDataSource();
        Connection conn = ds.getConnection();
           LOG.debug("PROD JDBC OPEN {}", System.identityHashCode(conn));
        return conn;
    }

    /**
     * Vérifie que le DataSource est bien injecté.
     * @return DataSource non null
     * @throws IllegalStateException si non injecté
     */
    private DataSource ensureDataSource() {
        if (dataSource == null) {
            String msg = "DataSource non injecté pour PROD DB ! Vérifie le JNDI: java:jboss/datasources/golflc";
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
        return dataSource;
    }
}
