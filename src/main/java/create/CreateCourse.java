package create;

import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de création de Course
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 */
@ApplicationScoped
public class CreateCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Crée un Course dans la base de données
     * 
     * @param course Le parcours à créer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean create(final Course course) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        try (Connection conn = dataSource.getConnection()) {
            
            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false");
            
            // ✅ PARTIE NON MODIFIÉE - DÉBUT
            
            // Validation
            if (course == null) {
                msg = "Course cannot be null";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                msg = "Course name is required";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (course.getClub_idclub() == 0 ) {
                msg = "Course must be associated with a club";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Creating course: {}", course.toString());
            
            // Insert Course
            String query = LCUtil.generateInsertQuery(conn, "course");
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateCourse.mapCreate(ps, course);
                LCUtil.logps(ps);
                
                int row = ps.executeUpdate();
                
                if (row == 0) {
                    msg = "Fatal Error: No row inserted in " + methodName;
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
            }
            
            // Récupération de l'ID généré
            int generatedId = LCUtil.generatedKey(conn);
            course.setIdcourse(generatedId);
            
            msg = String.format("Course created: %s (ID: %d)", 
                               course.getCourseName(), 
                               course.getIdcourse());
            LOG.debug(msg);
           // LCUtil.showMessageInfo(msg);
            
            conn.commit();
            LOG.debug("Course creation committed successfully");
            
            // ✅ PARTIE NON MODIFIÉE - FIN
            
            return true;
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = "SQLException in " + methodName + ": " + sqle.getMessage();
            LOG.error(msg);
            throw sqle;
            
        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }
}


/*package create;
import entite.Club;
import entite.Course;
import static exceptions.LCException.*;
//import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.DATE_BEGIN_COURSE;
import static interfaces.GolfInterface.DATE_END_COURSE;
import static interfaces.Log.LOG;
import module java.sql;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Timestamp;
import java.time.Instant;
import connection_package.DBConnection;
import utils.LCUtil;

public class CreateCourse {
     
    public boolean create(final Club club, final Course course, final Connection conn) throws SQLException    {
        PreparedStatement ps = null;
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
              LOG.debug("...entering createCourse");
              LOG.debug("club ID  = " + club.getIdclub());
              LOG.debug("course Name  = " + course.getCourseName());
              LOG.debug("course Holes  = " + course.getCourseHoles());
              LOG.debug("course Par  = " + course.getCoursePar());
            final String query = LCUtil.generateInsertQuery(conn, "course");
         //   final String query = sql.sql.generateInsertQuery(conn, "course"); // new 16/12/2025
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, course.getCourseName());
            ps.setShort(3,(short)18); // mod 12-11-2018 toujours 18 holes for a course
            ps.setShort(4,course.getCoursePar());
    //        ps.setString(5, "M");   // nto clean up : gender is now a tee attribute
            ps.setInt(5, club.getIdclub());
    // dates standards pour tous les courses
    // provisoirement, changer ensuite les dates réelles via HeidiSQL
            ps.setTimestamp(6, Timestamp.valueOf(DATE_BEGIN_COURSE)); // date de début fictive pour tous les parcours
            ps.setTimestamp(7, Timestamp.valueOf(DATE_END_COURSE)); // date de fin fictive pour tous les parcours
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
             //    String p = ps.toString();
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                course.setIdcourse(LCUtil.generatedKey(conn));
//                tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
                String msg = "Course Created = " + course.getIdcourse() + "</h1>"
                        // + "<br/>name club = " + club.getClubName()
                        + "<br/>id club = " + club.getIdclub()
                        + "<br/>name course = " + course.getCourseName()
                        + "<br/>holes = " + course.getCourseHoles()
                        + "<br/>par = " + course.getCoursePar();
     //                   + "<br/>Gender = " + course.getCourseGender();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR insert for course : "
                        + course.getIdcourse()
                        // + "<br/>name club = " + club.getClubName()
                        + "<br/>id club = " + club.getIdclub()
                        + "<br/>Name course = " + course.getCourseName()
                        + "<br/>Holes = " + course.getCourseHoles()
                        + "<br/>par = " + course.getCoursePar();
       //                 + "<br/>Gender = " + course.getCourseGender();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
   }catch (SQLException e) {
        handleSQLException(e, methodName);
        return false;
   } catch (Exception e) {
        handleGenericException(e, methodName);
        return false;
   } finally {
           // DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method
} //end 
*/