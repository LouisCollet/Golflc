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
/*
import entite.Club;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import utils.LCUtil;

public class DeleteClub implements interfaces.GolfInterface{


  public boolean delete(final Club club, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
      PreparedStatement ps = null;
try{
    LOG.debug("starting");
        LOG.debug(" CASCADING DELETE ATTENTION ! for club "  + club); // new 15-02-2021
        // voir autre methode !!
    final String query = """
        DELETE from club
        WHERE club.idclub = ?
       """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub());
    LCUtil.logps(ps);
    int row_delete = ps.executeUpdate();
        LOG.debug("deleted Club = {}", row_delete);
    String msg = "There are " + row_delete + " Club deleted = " + club;
        LOG.debug(msg);
  //      LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String error = "SQL Exception in DeleteClub = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteClub() " + ex;
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}finally{
        connection_package.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

  public boolean deleteClubAndChilds(final Club club,final Connection conn) throws Exception{
       final String methodName = utils.LCUtil.getCurrentMethodName();
    PreparedStatement ps = null;
try{
   // nez fonctionne pas !!
        /* encore à faire : payments-cotisation, greenfee, creditcard, activation

 //    prb si player a un PlayerRole admin (local administrateur)
  //  SQL Exception in delete.DeletePlayer.deletePlayerAndChilds / java.sql.SQLIntegrityConstraintViolationException:
  //  Cannot delete or update a parent row: a foreign key constraint fails
//    (`golflc`.`club`, CONSTRAINT `club_existe_local_admin` FOREIGN KEY (`ClubLocalAdmin`)
//    REFERENCES `player` (`idplayer`)), SQLState = 23000, ErrorCode = 1451
//    solution insert value null dans ClubLocalAdmin
//
     LOG.debug("starting");
     LOG.debug("for club = {}", club);
     // on commende par le niveau le plus bas !

     final String query = """
          DELETE from course
          WHERE course.club_idclub = ?
         """;
    ps = conn.prepareStatement(query);
  //  ps.setInt(1, club.getIdplayer());
    LCUtil.logps(ps);
    int row_hcp = ps.executeUpdate();
        LOG.debug("deleted handicap EGA = {}", row_hcp);

  /*



  final String query = """
               DELETE from score
               WHERE score.inscription_player_idplayer = ?
            """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_score = ps.executeUpdate();
        LOG.debug("deleted score = {}", row_score);

    query = """
             DELETE from inscription
             WHERE InscriptionIdPlayer = ?
          """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_inscription = ps.executeUpdate();
        LOG.debug("deleted inscription = {}", row_inscription);



    query = """
             DELETE from handicap_index
             WHERE HandicapPlayerId = ?
            """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_hcp_index = ps.executeUpdate();
        LOG.debug("deleted Handicap Index WHS = {}", row_hcp_index);

    query = """
            DELETE from blocking
            WHERE BlockingPlayerId = ?
           """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_blocking = ps.executeUpdate();
        LOG.debug("deleted blocking = {}", row_blocking);

    query = """
            DELETE from audit
            WHERE AuditPlayerId = ?
            """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_audit = ps.executeUpdate();
        LOG.debug("deleted audit = {}", row_audit);

    query = """
            DELETE from payments_subscription
            WHERE SubscriptionIdPlayer = ?
          """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_subscription = ps.executeUpdate();
        LOG.debug("deleted subscription = {}", row_subscription);

    query = """
           DELETE from lesson
           WHERE EventPlayerId = ?
          """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_schedule = ps.executeUpdate();
        LOG.debug("deleted schedule = {}", row_schedule);


    query = """
            DELETE elete from player
            WHERE player.idplayer = ?
          """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps);
    int row_player = ps.executeUpdate();
        LOG.debug("deleted player = {}", row_player);





 //   String msg = "<br/> <h1>Records deleted = "
 //                       + " <br/></h1>player = " + player.getIdplayer()
 //                       + " <br/>score = " + row_score
 //                       + " <br/>inscription = " + row_inscription
 //                       + " <br/>handicap = " + row_hcp
 //                       + " <br/>handicap Index = " + row_hcp_index
  //                      + " <br/>blocking = " + row_blocking
 //                       + " <br/>player = " + row_player;
 //          LOG.debug(msg);
    //    LCUtil.showMessageInfo(msg);
        return true;

}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
     //   utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method


 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     LOG.debug("entering main with conn = {}", conn);
     Club club = new Club();
     club.setIdclub(1122);
     boolean b = new DeleteClub().delete(club, conn);
   // boolean b = new DeleteClub().deleteClubAndChilds(club, conn);
    LOG.debug("from main - resultat deleteclub = {}", b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     //  DBConnection.closeQuietly(conn, null, null, null);
          }
} // end method main
} //end class
*/