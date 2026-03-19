package connection_package;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;

import static interfaces.Log.LOG;

/**
 * Implémentation TEST de ConnectionProvider.
 * Utilisée par Weld SE pour les tests d’intégration.
 *
 * La responsabilité de fermer la Connection
 * reste du côté DAO (try-with-resources).
 */
@ApplicationScoped
@TestDB
public class TestConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() {
        try {
            Connection conn = DBConnection2.getConnection();
            LOG.debug("TEST JDBC OPEN {}", System.identityHashCode(conn));
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Erreur JDBC TEST", e);
        }
    }
}
