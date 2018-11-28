package create;
import entite.Club;
import entite.Course;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
/**
 *
 * @author collet
 */
public class CreateCourse implements interfaces.Log, interfaces.GolfInterface
{
    public boolean createCourse(final Club club, final Course course, final Connection conn) throws SQLException
    {
        PreparedStatement ps = null;
        try {
            LOG.info("...entering createCourse");
            LOG.info("club ID  = " + club.getIdclub());
            //LOG.info("club City  = " + club.getClubCity() );
            LOG.info("course Name  = " + course.getCourseName());
            LOG.info("course Holes  = " + course.getCourseHoles());
            LOG.info("course Par  = " + course.getCoursePar());
   //         LOG.info("course Gender  = " + course.getCourseGender() );

  //          conn = DBConnection.getConnection();
            final String query = LCUtil.generateInsertQuery(conn, "course"); // new 15/11/2012
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, course.getCourseName());
                    //    ps.setShort(3, course.getCourseHoles());
            ps.setShort(3, (short)18); // mod 12-11-2018 toujours 18 holes for a course

            ps.setShort(4, course.getCoursePar());
    //        ps.setString(5, "M");   // nto clean up : gender is now a tee attribute
            ps.setInt(5, club.getIdclub());
    // dates standards pour tous les courses
    // provisoirement, changer ensuite les dates réelles via HeidiSQL
            ps.setString(6, DATE_BEGIN_COURSE); // date de début fictive pour tous les parcours
            ps.setString(7, DATE_END_COURSE); // date de fin fictive pour tous les parcours
            ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
             //    String p = ps.toString();
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if (row != 0) 
            {
                int key = LCUtil.generatedKey(conn);
                    LOG.info("Course created = " + key);
                course.setIdcourse(key);
//                tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
                String msg = "<br/><br/><h1>Course Created = " + course.getIdcourse() + "</h1>"
                        // + "<br/>name club = " + club.getClubName()
                        + "<br/>id club = " + club.getIdclub()
                        + "<br/>name course = " + course.getCourseName()
                        + "<br/>holes = " + course.getCourseHoles()
                        + "<br/>par = " + course.getCoursePar();
     //                   + "<br/>Gender = " + course.getCourseGender();
                LOG.info(msg);
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
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }
 //       catch (MySQLIntegrityConstraintViolationException cv) {
 //           String msg = "£££ MySQLIntegrityConstraintViolationException in insert Course = " + cv.getMessage();
 //           LOG.error(msg);
 //           LCUtil.showMessageFatal(msg);
 //           return false;
 //       }
        catch (SQLException sqle) {
            //LOG.error("-- SQLException in Insert Course " + sqle.toString());
            String msg = "SQLException in Insert Course = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (NumberFormatException nfe) {
            //LOG.error("-- £££ NumberFormatException in Insert Course " + nfe.toString());
            String msg = "£££ NumberFormatException in Insert Course = " + nfe.getMessage();
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

}
