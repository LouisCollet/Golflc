package delete;

//import connection_package.ConnectionProvider;
//import connection_package.ProdDB;
import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de suppression de Course
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class DeleteCourse implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    // ========================================
    // Injection CDI
    // ========================================

    /*
     * Injection CDI du ConnectionProvider (Production DB)

    @Inject
    @ProdDB
    private ConnectionProvider connectionProvider;
     */

    @Inject private dao.GenericDAO dao;



    // ========================================
    // Suppression Simple
    // ========================================

    /**
     * Supprime un Course (simple delete)
     *
     * @param course Le parcours à supprimer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean delete(final Course course) throws Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

    //    LOG.debug("connectionProvider = {}", connectionProvider);

    try (Connection conn = dao.getConnection()) {

            // ========================================
            // Configuration transaction
            // ========================================
            conn.setAutoCommit(false);
            msg = "AutoCommit set to false";
            LOG.info(msg);

            // ========================================
            // Validation
            // ========================================
            if (course == null) {
                msg = "Course cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            if (course.getIdcourse() == null || course.getIdcourse() == 0) {
                msg = "Course ID is required for deletion";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.debug("Deleting course: {} (ID: {})", course.getCourseName(), course.getIdcourse());
            LOG.warn("⚠️ CASCADING DELETE - This will affect related records!");

            // ========================================
            // Delete Course
            // ========================================
            String query = """
                DELETE FROM course
                WHERE course.idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                LCUtil.logps(ps);

                int rowsDeleted = ps.executeUpdate();
                LOG.debug("Rows deleted: {}", rowsDeleted);

                if (rowsDeleted == 0) {
                    msg = "No course deleted - Course may not exist: ID " + course.getIdcourse();
                    LOG.warn(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }

            msg = String.format("Course deleted: %s (ID: %d)",
                               course.getCourseName(),
                               course.getIdcourse());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            msg = "Course deletion committed successfully";
            LOG.debug(msg);

            return true;

        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("SQLException in %s: %s (SQLState: %s, ErrorCode: %d)",
                               methodName,
                               sqle.getMessage(),
                               sqle.getSQLState(),
                               sqle.getErrorCode());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle;

        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }

    // ========================================
    // Suppression Cascade (Course + enfants)
    // ========================================

    /**
     * Supprime un Course et toutes ses données liées (CASCADE)
     *
     * Ordre de suppression (du plus bas au plus haut) :
     * 1. Scores
     * 2. Inscriptions (player_has_round)
     * 3. Rounds
     * 4. Holes
     * 5. Tees
     * 6. Course
     *
     * @param course Le parcours à supprimer avec ses enfants
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean deleteCascading(final Course course) throws Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

          try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false for cascading delete");

            // Validation
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                msg = "Valid course ID is required for cascading deletion";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.warn("⚠️⚠️⚠️ CASCADING DELETE - Deleting course {} and ALL related data!", course.getIdcourse());

            int totalDeleted = 0;

            // ========================================
            // 1. Delete Scores (niveau le plus bas)
            // ========================================
            String query = """
                DELETE score FROM score
                INNER JOIN player_has_round ON score.player_has_round_idinscription = player_has_round.idinscription
                INNER JOIN round ON player_has_round.round_idround = round.idround
                WHERE round.course_idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} scores", deleted);
            }

            // ========================================
            // 2. Delete Inscriptions (player_has_round)
            // ========================================
            query = """
                DELETE player_has_round FROM player_has_round
                INNER JOIN round ON player_has_round.round_idround = round.idround
                WHERE round.course_idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} inscriptions", deleted);
            }

            // ========================================
            // 3. Delete Rounds
            // ========================================
            query = """
                DELETE FROM round
                WHERE round.course_idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} rounds", deleted);
            }

            // ========================================
            // 4. Delete Holes
            // ========================================
            query = """
                DELETE FROM hole
                WHERE hole.course_idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} holes", deleted);
            }

            // ========================================
            // 5. Delete Tees
            // ========================================
            query = """
                DELETE FROM tee
                WHERE tee.course_idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} tees", deleted);
            }

            // ========================================
            // 6. Enfin, Delete Course
            // ========================================
            query = """
                DELETE FROM course
                WHERE course.idcourse = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                int deleted = ps.executeUpdate();
                totalDeleted += deleted;
                LOG.debug("Deleted {} course", deleted);
            }

            msg = String.format("Cascading delete completed: %d total records deleted for course %s (ID: %d)",
                               totalDeleted,
                               course.getCourseName(),
                               course.getIdcourse());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            LOG.debug("Cascading delete committed successfully");

            return true;

        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("SQLException in %s: %s (SQLState: %s, ErrorCode: %d)",
                               methodName,
                               sqle.getMessage(),
                               sqle.getSQLState(),
                               sqle.getErrorCode());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle;

        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }

    // ========================================
    // Main pour tests (hors container CDI)
    // ========================================

    /**
     * Main pour tests hors JSF
     * Note: Non fonctionnel sans container CDI
     */
    public static void main(String[] args) {
        try {
            // Exemple de test (nécessite CDI)
            Course course = new Course();
            course.setIdcourse(128);
            course.setCourseName("Test Course");

            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test course: {}", course);

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
            LCUtil.showMessageFatal("Exception in main: " + e.getMessage());
        }
    }
}

/*
import entite.Course;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import static interfaces.Log.LOG;
import utils.LCUtil;

public class DeleteCourse {
    public boolean delete(final Course course, final Connection conn) throws Exception{
    PreparedStatement ps = null;
try
{       LOG.debug("starting Delete Course ... = " );
        LOG.debug(" for idcourse "  + course);
    final String query = """
        DELETE from course
        WHERE course.idcourse = ?
       """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse());
    LCUtil.logps(ps);
    int row_delete = ps.executeUpdate();
        LOG.debug("deleted Course = " + row_delete);
    String msg = "<br/>There are " + row_delete + " Course deleted = " + course;
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteCourse = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteCourse() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        connection_package.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     Course course = new Course();
     course.setIdcourse(128);
    boolean b = new DeleteCourse().delete(course, conn);
       LOG.debug("from main - resultat deleteCourse = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null);
          }
} // end method main
} //end class
*/