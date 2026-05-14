package integration.create;

import connection_package.JdbcConnectionProvider;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.Statement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Crée la table cart en DB — à exécuter une seule fois.
 * mvn failsafe:integration-test -Pfast-it -Dit.test=CreateCartTableIT
 */
@Tag("integration")
public class CreateCartTableIT {

    @Test
    void createCartTable_realDB_tableExists() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             Statement st = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS cart (
                    idCart        INT           NOT NULL AUTO_INCREMENT,
                    cartPlayerId  INT           NOT NULL,
                    cartClubId    INT           NOT NULL,
                    cartType      VARCHAR(20)   NOT NULL,
                    cartItemsJson JSON          NOT NULL,
                    cartTotal     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    cartStatus    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
                    cartCreatedAt DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    cartModificationDate DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (idCart),
                    UNIQUE KEY ukCartPlayerClubType (cartPlayerId, cartClubId, cartType),
                    CONSTRAINT fkCartPlayer FOREIGN KEY (cartPlayerId) REFERENCES player (idplayer),
                    CONSTRAINT fkCartClub   FOREIGN KEY (cartClubId)   REFERENCES club   (idclub)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

            assertDoesNotThrow(() -> {
                try { st.execute(sql); } catch (Exception e) { throw new RuntimeException(e); }
            });

            LOG.info("cart table created (or already existed)");
        }
    } // end method

} // end class
