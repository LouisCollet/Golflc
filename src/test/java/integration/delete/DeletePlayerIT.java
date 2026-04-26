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
 * Test d'intégration — supprime réellement le player de la base MySQL.
 * ⚠️ ATTENTION : supprime définitivement les données.
 * Changer PLAYER_ID avant d'exécuter.
 *
 * Prérequis : variables d'environnement MYSQL_USERNAME et MYSQL_PASSWORD définies.
 * Lancer avec : mvn failsafe:integration-test -Pfast-it -Dit.test=DeletePlayerIT
 */
@Tag("integration")
public class DeletePlayerIT {

    private static final int PLAYER_ID = 458929; // ← player à supprimer

    @Test
    void deletePlayerAndChilds_realDB_deletesAllRows() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rowScore        = execute(conn, "DELETE FROM score                 WHERE inscription_player_idplayer = ?");
                int rowInscription  = execute(conn, "DELETE FROM inscription      WHERE InscriptionIdPlayer = ?");
                int rowHcp          = execute(conn, "DELETE FROM handicap              WHERE player_idplayer = ?");
                int rowHcpIndex     = execute(conn, "DELETE FROM handicap_index        WHERE HandicapPlayerId = ?");
          //    int rowHcpCopy      = execute(conn, "DELETE FROM handicap_index_copy   WHERE HandicapPlayerId = ?");
                int rowBlocking     = execute(conn, "DELETE FROM blocking              WHERE BlockingPlayerId = ?");
                int rowAudit        = execute(conn, "DELETE FROM audit                 WHERE AuditPlayerId = ?");
                int rowSubscription = execute(conn, "DELETE FROM payments_subscription WHERE SubscriptionIdPlayer = ?");
                int rowLesson       = execute(conn, "DELETE FROM lesson                WHERE EventPlayerId = ?");
                int rowPlayer       = execute(conn, "DELETE FROM player                WHERE idplayer = ?");

                assertNotEquals(0, rowPlayer, "Player " + PLAYER_ID + " not exists");
                conn.commit();
                LOG.info("player={} deleted — score={} inscription={} hcp={} hcpIndex={} blocking={} audit={} subscription={} lesson={} player={}",
                        PLAYER_ID, rowScore, rowInscription, rowHcp, rowHcpIndex, rowBlocking, rowAudit, rowSubscription, rowLesson, rowPlayer);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, PLAYER_ID);
            return ps.executeUpdate();
        }
    } // end method

} // end class
