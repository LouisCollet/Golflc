
package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;

@ApplicationScoped
public class MySqlInfoProvider implements InfoProvider {

    @Override
    public String name() {
        return "MySQL";
    }

    @Override
    public String get() {
        try (Connection c = connection_package.DBConnection2.getConnection()) {
            return c.getMetaData().getDatabaseProductName() + " "
                 + c.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} // end class
