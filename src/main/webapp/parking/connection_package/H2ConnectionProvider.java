
package connection_package;

import connection_package.ConnectionProvider;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.sql.Connection;
import java.sql.DriverManager;

@Alternative
@Priority(1)
@ApplicationScoped
public class H2ConnectionProvider implements ConnectionProvider {

    private static final String URL =
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    @Override
    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, "sa", "");
    }
}
