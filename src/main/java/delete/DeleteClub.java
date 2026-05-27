package delete;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de suppression de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class DeleteClub implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteClub() { }

    // ========================================
    // Suppression Simple
    // ========================================

    /**
     * Supprime un Club (simple delete)
     *
     * @param club Le club à supprimer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean delete(final Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            msg = "AutoCommit set to false";
            LOG.info(msg);

            if (club == null) {
                msg = "Club cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            if (club.getIdclub() == null || club.getIdclub() == 0) {
                msg = "Club ID is required for deletion";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.debug("Deleting club: {} (ID: {})", club.getClubName(), club.getIdclub());
            LOG.warn("⚠️ CASCADING DELETE - This will affect related records!");

            String query = """
                DELETE FROM club
                WHERE club.idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                LCUtil.logps(ps);

                int rowsDeleted = ps.executeUpdate();
                LOG.debug("Rows deleted: {}", rowsDeleted);

                if (rowsDeleted == 0) {
                    msg = "No club deleted - Club may not exist: ID " + club.getIdclub();
                    LOG.warn(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }

            msg = String.format("Club deleted: %s (ID: %d)",
                               club.getClubName(),
                               club.getIdclub());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            conn.commit();
            msg = "Club deletion committed successfully";
            LOG.debug(msg);

            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // Suppression Cascade (Club + enfants)
    // ========================================

    /**
     * Supprime un Club et toutes ses données liées (CASCADE)
     *
     * Ordre de suppression (du plus bas au plus haut) :
     * 1. Holes
     * 2. Tees
     * 3. Scores
     * 4. Inscriptions (inscription)
     * 5. Rounds
     * 6. Courses
     * 7. Subscriptions/Payments liés au club
     * 8. Club
     *
     * @param club Le club à supprimer avec ses enfants
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean deleteCascading(final Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false for cascading delete");

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                msg = "Valid club ID is required for cascading deletion";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.warn("⚠️⚠️⚠️ CASCADING DELETE - Deleting club {} and ALL related data!", club.getIdclub());

            int totalDeleted = 0;

            // ========================================
            // 1. Delete Holes (niveau le plus bas)
            // ========================================
            String query = """
                DELETE hole FROM hole
                INNER JOIN course ON hole.course_idcourse = course.idcourse
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} holes", deleted);
            }

            // ========================================
            // 2. Delete Tees
            // ========================================
            query = """
                DELETE tee FROM tee
                INNER JOIN course ON tee.course_idcourse = course.idcourse
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} tees", deleted);
            }

            // ========================================
            // 3. Delete Scores
            // ========================================
            query = """
                DELETE score FROM score
                INNER JOIN inscription ON score.inscription_player_idplayer = inscription.InscriptionIdPlayer
                                      AND score.inscription_round_idround   = inscription.InscriptionIdRound
                INNER JOIN round ON inscription.round_idround = round.idround
                INNER JOIN course ON round.course_idcourse = course.idcourse
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} scores", deleted);
            }

            // ========================================
            // 4. Delete Inscriptions (inscription)
            // ========================================
            query = """
                DELETE inscription FROM inscription
                INNER JOIN round ON inscription.round_idround = round.idround
                INNER JOIN course ON round.course_idcourse = course.idcourse
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} inscriptions", deleted);
            }

            // ========================================
            // 5. Delete Rounds
            // ========================================
            query = """
                DELETE round FROM round
                INNER JOIN course ON round.course_idcourse = course.idcourse
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} rounds", deleted);
            }

            // ========================================
            // 6. Delete Courses
            // ========================================
            query = """
                DELETE FROM course
                WHERE course.club_idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} courses", deleted);
            }

            // ========================================
            // 7. Delete Subscriptions liées au club
            // ========================================
            query = """
                DELETE FROM payments_subscription
                WHERE SubscriptionClubId = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} subscriptions", deleted);
            }

            // ========================================
            // 8. Gérer le local admin (set NULL)
            // ========================================
            // Problème : si un player a le rôle d'admin local, on ne peut pas supprimer le club
            // Solution : mettre ClubLocalAdmin à NULL d'abord
            query = """
                UPDATE club
                SET ClubLocalAdmin = NULL
                WHERE idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                ps.executeUpdate();
                LOG.debug("Set ClubLocalAdmin to NULL");
            }

            // ========================================
            // 9. Enfin, Delete Club
            // ========================================
            query = """
                DELETE FROM club
                WHERE club.idclub = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} club", deleted);
            }

            msg = String.format("Cascading delete completed: %d total records deleted for club %s (ID: %d)",
                               totalDeleted,
                               club.getClubName(),
                               club.getIdclub());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            conn.commit();
            LOG.debug("Cascading delete committed successfully");

            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = new Club();
            club.setIdclub(1122);
            LOG.debug("Main ready (CDI required for execution)");
        } catch (Exception e) {
            LOG.error("Exception in main: {}", e.getMessage(), e);
        }
    } // end main
*/
} // end class
