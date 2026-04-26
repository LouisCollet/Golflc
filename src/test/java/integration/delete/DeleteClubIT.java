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
 * Test d'intégration — supprime réellement le club et tous ses enfants de la base MySQL.
 * ⚠️ ATTENTION : supprime définitivement les données.
 * Changer CLUB_ID avant d'exécuter.
 *
 * Prérequis : variables d'environnement MYSQL_USERNAME et MYSQL_PASSWORD définies.
 * Lancer avec : mvn failsafe:integration-test -Pfast-it -Dit.test=DeleteClubIT
 */
@Tag("integration")
public class DeleteClubIT {

    private static final int CLUB_ID = 0; // ← club à supprimer

    @Test
    void deleteClubAndChilds_realDB_deletesAllRows() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Niveau le plus bas en premier
                int rowScore        = execute(conn, """
                        DELETE FROM score WHERE (inscription_player_idplayer, inscription_round_idround) IN (
                            SELECT InscriptionIdPlayer, InscriptionIdRound FROM inscription WHERE round_idround IN (
                                SELECT idround FROM round WHERE course_idcourse IN (
                                    SELECT idcourse FROM course WHERE club_idclub = ?)))""");

                int rowInscription  = execute(conn, """
                        DELETE FROM inscription WHERE round_idround IN (
                            SELECT idround FROM round WHERE course_idcourse IN (
                                SELECT idcourse FROM course WHERE club_idclub = ?))""");

                int rowRound        = execute(conn, """
                        DELETE FROM round WHERE course_idcourse IN (
                            SELECT idcourse FROM course WHERE club_idclub = ?)""");

                int rowHole         = execute(conn, """
                        DELETE FROM hole WHERE course_idcourse IN (
                            SELECT idcourse FROM course WHERE club_idclub = ?)""");

                int rowTee          = execute(conn, """
                        DELETE FROM tee WHERE course_idcourse IN (
                            SELECT idcourse FROM course WHERE club_idclub = ?)""");

                int rowCourse       = execute(conn, "DELETE FROM course               WHERE club_idclub = ?");
                int rowSubscription = execute(conn, "DELETE FROM payments_subscription WHERE SubscriptionClubId = ?");

                // SET NULL sur ClubLocalAdmin avant delete club (contrainte FK)
                execute(conn,                       "UPDATE club SET ClubLocalAdmin = NULL WHERE idclub = ?");

                int rowClub         = execute(conn, "DELETE FROM club WHERE idclub = ?");

                assertNotEquals(0, rowClub, "Club " + CLUB_ID + " not exists");
                conn.commit();
                LOG.info("club={} deleted — score={} inscription={} round={} hole={} tee={} course={} subscription={} club={}",
                        CLUB_ID, rowScore, rowInscription, rowRound, rowHole, rowTee, rowCourse, rowSubscription, rowClub);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, CLUB_ID);
            return ps.executeUpdate();
        }
    } // end method

} // end class
