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
 * Test d'intégration — supprime réellement l'inscription de la base MySQL.
 * ⚠️ ATTENTION : supprime définitivement les données.
 * Changer PLAYER_ID et ROUND_ID avant d'exécuter.
 *
 * Prérequis : variables d'environnement MYSQL_USERNAME et MYSQL_PASSWORD définies.
 * Lancer avec : mvn failsafe:integration-test -Pfast-it -Dit.test=DeleteInscriptionIT
 */
@Tag("integration")
public class DeleteInscriptionIT {

    private static final int PLAYER_ID = 0; // ← player de l'inscription
    private static final int ROUND_ID  = 0; // ← round de l'inscription

    @Test
    void deleteInscription_realDB_deletesAllRows() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rowScore       = execute(conn, "DELETE FROM score             WHERE inscription_player_idplayer = ? AND inscription_round_idround = ?", PLAYER_ID, ROUND_ID);
                int rowInscription = execute(conn, "DELETE FROM inscription  WHERE InscriptionIdPlayer = ? AND InscriptionIdRound = ?",                          PLAYER_ID, ROUND_ID);
                int rowPayment     = execute(conn, "DELETE FROM payments_greenfee WHERE GreenfeeIdPlayer = ? AND GreenfeeIdRound = ?",                                 PLAYER_ID, ROUND_ID);

                assertNotEquals(0, rowInscription, "Inscription player=" + PLAYER_ID + " round=" + ROUND_ID + " not exists");
                conn.commit();
                LOG.info("inscription deleted — player={} round={} score={} inscription={} payment={}",
                        PLAYER_ID, ROUND_ID, rowScore, rowInscription, rowPayment);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int execute(Connection conn, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    } // end method

} // end class
