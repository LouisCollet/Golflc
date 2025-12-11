package create;
import entite.Club;
import entite.Course;
import static interfaces.GolfInterface.DATE_BEGIN_COURSE;
import static interfaces.GolfInterface.DATE_END_COURSE;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class CreateCourse {
    
    public boolean create(final Club club, final Course course, final Connection conn) throws SQLException    {
        PreparedStatement ps = null;
        try {
              LOG.debug("...entering createCourse");
              LOG.debug("club ID  = " + club.getIdclub());
              LOG.debug("course Name  = " + course.getCourseName());
              LOG.debug("course Holes  = " + course.getCourseHoles());
              LOG.debug("course Par  = " + course.getCoursePar());
            final String query = LCUtil.generateInsertQuery(conn, "course"); // new 15/11/2012
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, course.getCourseName());
            ps.setShort(3,(short)18); // mod 12-11-2018 toujours 18 holes for a course
            ps.setShort(4,course.getCoursePar());
    //        ps.setString(5, "M");   // nto clean up : gender is now a tee attribute
            ps.setInt(5, club.getIdclub());
    // dates standards pour tous les courses
    // provisoirement, changer ensuite les dates réelles via HeidiSQL
          //  ps.setString(6, DATE_BEGIN_COURSE); // date de début fictive pour tous les parcours
            ps.setTimestamp(6, Timestamp.valueOf(DATE_BEGIN_COURSE)); // date de début fictive pour tous les parcours
         //   ps.setString(7, DATE_END_COURSE); // date de fin fictive pour tous les parcours
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
   }catch (SQLException sqle) {
            //LOG.error("-- SQLException in Insert Course " + sqle.toString());
            String msg = "SQLException in Insert Course = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
    } catch (Exception e) {
            String msg = "£££ Exception in CreateCourse = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        //return null;
        } finally {
           // DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method
} //end 