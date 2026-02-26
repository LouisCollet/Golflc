
package connection_package;
import static interfaces.Log.LOG;
import java.sql.Connection;
/**
 * Le JdbcConnectionProvider fournit une Connection.
 * Le DAO est RESPONSABLE de la fermer.
 */
public class SimpleJdbcConnectionProducer implements ConnectionProvider {
    @Override
    public Connection getConnection() throws Exception {
        LOG.debug("JDBC OPEN {}", System.identityHashCode(DBConnection2.getConnection()));
        return DBConnection2.getConnection();
    }
}