package connection_package;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@ProdDB
@ApplicationScoped
public class JdbcConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws Exception {
        LOG.debug("Opening JDBC connection");

        Properties props = findProperties();
    //    printProperties(props);

        String jdbcUrl =
                props.getProperty("jdbc.mysql")
              + props.getProperty("jdbc.host")
              + props.getProperty("jdbc.dbname")
              + props.getProperty("jdbc.params");

        LOG.debug("JDBC URL = {}", jdbcUrl);

        String username = System.getenv("MYSQL_USERNAME");
        String password = System.getenv("MYSQL_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException(
                "Environment variables MYSQL_USERNAME or MYSQL_PASSWORD are not set");
        }

        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private Properties findProperties() throws Exception {
        Properties props = new Properties();

        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("jdbc.properties")) {

            if (is == null) {
                throw new IllegalStateException("jdbc.properties not found in classpath");
            }

            props.load(is);
        }

        return props;
    }

    private void printProperties(Properties props) {
        LOG.debug("Loaded JDBC properties:");
        props.forEach((k, v) -> LOG.debug("{} = {}", k, v));
    }
}
