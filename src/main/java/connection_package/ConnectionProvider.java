
package connection_package;

import java.sql.Connection;
/**
 * Le JdbcConnectionProvider fournit une Connection.
 * Le DAO est RESPONSABLE de la fermer.
 */
public interface ConnectionProvider {
    Connection getConnection() throws Exception;
}