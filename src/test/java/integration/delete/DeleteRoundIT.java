package integration.delete;

import connection_package.JdbcConnectionProvider;
import static interfaces.Log.LOG;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration — supprime réellement le round de la base MySQL.
 * ⚠️ ATTENTION : supprime définitivement les données.
 * Changer ROUND_ID avant d'exécuter.
 *
 * Prérequis : variables d'environnement MYSQL_USERNAME et MYSQL_PASSWORD définies.
 * Lancer avec : mvn failsafe:integration-test -Pfast-it -Dit.test=DeleteRoundIT
 */
@Tag("integration")
public class DeleteRoundIT {

    private static final int ROUND_ID = 776; // ← round à supprimer

    @Test
    void deleteRoundAndChilds_realDB_deletesAllRows() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rowScore        = execute(conn, "DELETE FROM score             WHERE inscription_round_idround = ?");
                int rowInscription  = execute(conn, "DELETE FROM inscription  WHERE InscriptionIdRound = ?");
                int rowPayment      = execute(conn, "DELETE FROM payments_greenfee WHERE GreenfeeIdRound = ?");
                int rowRound        = execute(conn, "DELETE FROM round             WHERE idround = ?");

                assertNotEquals(0, rowRound, "Round " + ROUND_ID + " not exists");
                conn.commit();
                LOG.info("round={} deleted — score={} inscription={} payment={} round={}",
                        ROUND_ID, rowScore, rowInscription, rowPayment, rowRound);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ROUND_ID);
            return ps.executeUpdate();
        }
    } // end method

} // end class
