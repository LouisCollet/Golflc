package delete;

import entite.Player;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de suppression de Player et ses enfants
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class DeletePlayer implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Supprime un Player et tous ses enfants (cascading delete)
     *
     * @param player Le player à supprimer
     * @return true si succès
     * @throws Exception en cas d'erreur
     */
    public boolean deletePlayerAndChilds(final Player player) throws Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            LOG.debug("AutoCommit set to false");

            // Validation
            if (player == null || player.getIdplayer() == null || player.getIdplayer() == 0) {
                msg = "Player ID is required for deletion";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            // ✅ PARTIE NON MODIFIÉE - DÉBUT

            /* encore à faire : payments-cotisation, greenfee, creditcard, activation

             prb si player a un PlayerRole admin (local administrateur)
            SQL Exception in delete.DeletePlayer.deletePlayerAndChilds / java.sql.SQLIntegrityConstraintViolationException:
            Cannot delete or update a parent row: a foreign key constraint fails
            (`golflc`.`club`, CONSTRAINT `club_existe_local_admin` FOREIGN KEY (`ClubLocalAdmin`)
            REFERENCES `player` (`idplayer`)), SQLState = 23000, ErrorCode = 1451
            solution insert value null dans ClubLocalAdmin
            */
            LOG.debug("starting " + methodName);
            // on commende par le niveau le plus bas !

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from score
                    WHERE score.player_has_round_player_idplayer = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_score = ps.executeUpdate();
                LOG.debug("deleted score = " + row_score);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from player_has_round
                    WHERE InscriptionIdPlayer = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_inscription = ps.executeUpdate();
                LOG.debug("deleted inscription = " + row_inscription);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from handicap
                    WHERE handicap.player_idplayer = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_hcp = ps.executeUpdate();
                LOG.debug("deleted handicap EGA = " + row_hcp);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from handicap_index
                    WHERE HandicapPlayerId = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_hcp_index = ps.executeUpdate();
                LOG.debug("deleted Handicap Index WHS = " + row_hcp_index);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from blocking
                    WHERE BlockingPlayerId = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_blocking = ps.executeUpdate();
                LOG.debug("deleted blocking = " + row_blocking);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from audit
                    WHERE AuditPlayerId = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_audit = ps.executeUpdate();
                LOG.debug("deleted audit = " + row_audit);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from payments_subscription
                    WHERE SubscriptionIdPlayer = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_subscription = ps.executeUpdate();
                LOG.debug("deleted subscription = " + row_subscription);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from lesson
                    WHERE EventPlayerId = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_schedule = ps.executeUpdate();
                LOG.debug("deleted schedule = " + row_schedule);
            }

            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE from player
                    WHERE player.idplayer = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);
                int row_player = ps.executeUpdate();
                LOG.debug("deleted player = " + row_player);
            }

        /*   String msg = "<br/> <h1>Records deleted = "
                            + " <br/></h1>player = " + player.getIdplayer()
                            + " <br/>score = " + row_score
                            + " <br/>inscription = " + row_inscription
                            + " <br/>handicap = " + row_hcp
                            + " <br/>handicap Index = " + row_hcp_index
                            + " <br/>blocking = " + row_blocking
                            + " <br/>player = " + row_player;
               LOG.debug(msg);
            //    LCUtil.showMessageInfo(msg);
        */

            // ✅ PARTIE NON MODIFIÉE - FIN

            conn.commit();
            msg = "Player and all childs deleted successfully: ID " + player.getIdplayer();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);

            return true;

        } catch (SQLException e) {
            msg = "SQL Exception in " + methodName + e.toString()
                + ", SQLState = " + e.getSQLState()
                + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;

        } catch (Exception ex) {
            msg = "Exception in " + methodName + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw ex;
        }
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            // seule méthode utilisée !! pas accessible via application
            Player player = new Player();
            player.setIdplayer(111111);

            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test player ID: {}", player.getIdplayer());

        } catch (Exception e) {
            // ££ Exception in main
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
        }
    }
}