package connection_package;

import java.sql.Connection;

public class ManualJdbcConnectionProducer implements ConnectionProvider {

    private final Connection connection;

    public ManualJdbcConnectionProducer(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
